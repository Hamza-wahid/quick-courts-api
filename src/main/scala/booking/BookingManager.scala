package booking

import java.time.LocalTime

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import booking.messages.BookingManagerMessages._
import core.authorisation.JwtAuth.{generateToken, getMinutesPermittedPerDay}
import java.time.temporal.ChronoUnit.MINUTES

import booking.Constants._
import booking.messages.BookingManagerMessages.BookingRequestResult._
import booking.requests.BookingRequests.{CreateBookingRequest, _}
import BookingUtils._

import scala.concurrent.{ExecutionContext, Future}

class BookingManager(bookingDB: BookingRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {


  override def receive: Receive = {

    case GetBookingsByDate(BookingsByDateRequest(date)) =>
      log.info(GetBookingsLog(date))
      bookingDB
        .getByDate(date)
        .mapTo[Seq[Booking]]
        .pipeTo(sender())


    case GetBooking(id, userId, membershipType) =>
      log.info(GetBookingLog(id))
      val token = generateToken(userId, membershipType)

      bookingDB.getByBookingId(id)
        .map(handleGetBooking(token, _))
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

    case CreateBooking(userId, membershipType, CreateBookingRequest(courtNumber,startDateTime, endDateTime)) =>
      val date = extractDate(startDateTime)
      val startTime = extractTime(startDateTime)
      val endTime = extractTime(endDateTime)

      log.info(CreateBookingLog(userId, date, startTime, endTime))

      val booking = Booking(None,userId, courtNumber, date, startTime, endTime)
      val token = generateToken(userId, membershipType)

      val bookingRequestResultFuture = for {
        allBookingsOnDate <- bookingDB.getByDate(date)
        (isAvailable, isPermitted) = isBookingEligible(allBookingsOnDate, userId, membershipType, courtNumber, startTime, endTime)
        bookingRequestResult <- handleBookingEligibility(isAvailable, isPermitted, booking, token)
      } yield bookingRequestResult

      bookingRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

    case CancelBooking(userId, membershipType, id) =>
      log.info(CancelBookingLog(userId, id))
      val token = generateToken(userId, membershipType)

      val cancellationRequestResultFuture = for {
        bookingOption <- bookingDB.getByBookingId(id)
        bookingRequestResult <- handleCancellationRequest(bookingOption,id,token, userId)
      } yield bookingRequestResult

      cancellationRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

    case ModifyBooking(id, userId, membershipType, newCourtNumber) =>
      log.info(ModifyBookingLog(id, newCourtNumber))
      val token = generateToken(userId, membershipType)
      val modificationRequestResultFuture = bookingDB.getByBookingId(id).flatMap {
        case None =>
          log.info(BookingDoesNotExistLog(id))
          Future.successful(BookingDoesNotExist(token))
        case Some(booking) =>
          log.info(BookingExistsLog(id))
           for {
            bookings <- bookingDB.getByDateAndCourtNumber(booking.date, newCourtNumber)
            isAvailable = isSlotAvailable(bookings, booking.startTime, booking.endTime)
            updateRequestResult <- handleModificationRequest(booking, newCourtNumber, isAvailable, token, userId)
          } yield updateRequestResult

      }

      modificationRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

  }

  private def isBookingEligible(allBookingsOnDate: Seq[Booking],
                    userId: Long,
                    membershipType: Int,
                    courtNumber: Int,
                    startTime: String,
                    endTime: String): (Boolean, Boolean) = {
    val isAvailable = isSlotAvailable(allBookingsOnDate.filter(_.courtNumber == courtNumber), startTime, endTime)
    val isPermitted = isBookingPermitted(allBookingsOnDate.filter(_.userId == userId), membershipType, startTime.until(endTime, MINUTES))
    (isAvailable, isPermitted)
  }




  private def isSlotAvailable(bookings: Seq[Booking],
                              proposedStartTime: LocalTime,
                              proposedEndTime: LocalTime): Boolean = {
    bookings.forall { booking =>
      isAvailable(proposedStartTime, proposedEndTime, booking.startTime, booking.endTime)
    }
  }

  private def isAvailable(proposedStartTime: LocalTime,
                          proposedEndTime: LocalTime,
                          currentStartTime: LocalTime,
                          currentEndTime: LocalTime ) = {

    val startTimeAvailable = !((proposedStartTime.isAfter(currentStartTime) && proposedStartTime.isBefore(currentEndTime)) || proposedStartTime == currentStartTime)
    val endTimeAvailable = !((proposedEndTime.isAfter(currentStartTime) && proposedEndTime.isBefore(currentEndTime)) || proposedEndTime == currentEndTime)

    startTimeAvailable && endTimeAvailable
  }

  private def isBookingPermitted(bookings: Seq[Booking],
                                 membershipType: Int,
                                 bookingLength: Long): Boolean = {

    val minutesCurrentlyBooked = bookings.map { booking =>
      booking.startTime.until(booking.endTime,MINUTES)
    }.sum
    (bookingLength + minutesCurrentlyBooked) <= getMinutesPermittedPerDay(membershipType)
  }

  private def handleBookingEligibility(isSlotAvailable: Boolean,
                             isUserAuthorised: Boolean,
                             booking: Booking,
                             token: String): Future[BookingRequestResult] = {

    log.info(SlotEligibilityLog(isSlotAvailable, isUserAuthorised))

    (isSlotAvailable, isUserAuthorised) match {
    case (true, true) =>
      log.info(AddingBookingLog)
      bookingDB.insert(booking).map( booking => BookingSuccessful(booking.id.get,token))
    case (false, _) =>
      log.warning(SlotNotAvailableLog)
      Future.successful(SlotNotAvailable(token))
    case (_, false) =>
      log.warning(UserExceededTimeLog)
      Future.successful(UserNotAuthorised(token))
    }
  }

  private def handleModificationRequest(booking: Booking,
                                        newCourtNumber: Int,
                                        slotAvailable: Boolean,
                                        token: String,
                                        userId: Long): Future[BookingRequestResult] = {
    if (booking.userId != userId) {
      log.warning(UpdateNotAuthorizedLog(booking.userId, userId, booking.id.get))
      Future.successful(UserNotAuthorised(token))
    }
    else if (slotAvailable) {
      log.info(UpdatingBookingLog(booking.id.get))
      bookingDB.update(booking.id.get, newCourtNumber).map(_ => UpdateSuccessful(token))
    }
    else {
      log.warning(SlotNotAvailableLog)
      Future.successful(SlotNotAvailable(token))
    }
  }

  private def handleCancellationRequest(bookingOption: Option[Booking], id: Long, token: String, userId: Long): Future[BookingRequestResult] = bookingOption match {
    case Some(booking) =>
      if (booking.userId != userId) {
        log.warning(CancellationNotAuthorizedLog(booking.userId, userId, id))
        Future.successful(UserNotAuthorised(token))
      }
      else {
        log.info(DeletingBookingLog(id))
        bookingDB.delete(id).map(_ => CancellationSuccessful(token))
      }
    case None =>
      log.warning(BookingDoesNotExistLog(id))
      Future.successful(BookingDoesNotExist(token))
  }

  private def handleGetBooking(token: String, bookingOption: Option[Booking]): BookingRequestResult = bookingOption match {
    case None => BookingDoesNotExist(token)
    case  Some(booking) =>
      BookingExists(token,
        BookingResponse(
          booking.courtNumber,
          booking.date,
          booking.startTime,
          booking.endTime
        )
      )
  }
}
