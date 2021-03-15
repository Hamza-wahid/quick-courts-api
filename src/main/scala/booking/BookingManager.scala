package booking

import java.time.LocalTime

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import booking.messages.BookingManagerMessages._
import core.authorisation.JwtAuthUtils.{generateToken, getMinutesPermittedPerDay}
import java.time.temporal.ChronoUnit.MINUTES

import booking.Constants._
import booking.messages.BookingManagerMessages.BookingRequestResult._
import booking.requests.BookingRequests._
import BookingUtils._
import authentication.{User, UserRepo}

import scala.concurrent.{ExecutionContext, Future}

class BookingManager(bookingDB: BookingRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {


  override def receive: Receive = {

    case GetBookingsByDate(BookingsByDateRequest(date)) =>
      log.info(RetrievingBookings(date))
      bookingDB
        .getByDate(date)
        .mapTo[Seq[Booking]]
        .pipeTo(sender())


    case GetBooking(bookingId, userId, membershipType) =>
      log.info(RetrievingBooking(bookingId))

      val token = generateToken(userId, membershipType)

      bookingDB.getByBookingId(bookingId)
        .map(handleGetBooking(token, _))
        .mapTo[BookingRequestResult]
        .pipeTo(sender())


    case CreateBooking(userId, membershipType, CreateBookingRequest(courtNumber,startDateTime, endDateTime)) =>
      val date = extractDate(startDateTime)
      val startTime = extractTime(startDateTime)
      val endTime = extractTime(endDateTime)

      log.info(s"Request by user: $userId to book on the date $date between $startTime and $endTime ")

      val booking = Booking(None,userId, courtNumber, date, startTime, endTime)
      val token = generateToken(userId, membershipType)

      val bookingRequestResultFuture = for {
        allBookingsOnDate <- bookingDB.getByDate(date)
        (isAvailable, isPermitted) = isBookingAuthorised(allBookingsOnDate, userId, membershipType, courtNumber, startTime, endTime)
        bookingRequestResult <- handleBookingAuthorizedResponse(isAvailable, isPermitted, booking, token)
      } yield bookingRequestResult

      bookingRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

    case CancelBooking(userId, membershipType, id) =>
      log.info(s"Request by $userId to cancel booking: $id")
      val token = generateToken(userId, membershipType)

      val cancellationRequestResultFuture = for {
        bookingOption <- bookingDB.getByBookingId(id)
        bookingRequestResult <- handleCancellationRequest(bookingOption,id,token)
      } yield bookingRequestResult

      cancellationRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())

    case ModifyBooking(id, newCourtNumber) =>
      log.info("Modify request")
      val modificationRequestResultFuture = bookingDB.getByBookingId(id).flatMap {
        case None =>
          Future.successful(BookingDoesNotExist(""))
        case Some(booking) =>
          val startTime = convertToLocalTime(booking.startTime)
          val endTime = convertToLocalTime(booking.endTime)
           for {
            bookings <- bookingDB.getByDateAndCourtNumber(booking.date, newCourtNumber)
            isAvailable = isSlotAvailable(bookings, startTime, endTime)
            updateRequestResult <- perms(id, newCourtNumber, isAvailable)
          } yield updateRequestResult

      }

      modificationRequestResultFuture
        .mapTo[BookingRequestResult]
        .pipeTo(sender())


  }

  private def isBookingAuthorised(allBookingsOnDate: Seq[Booking],
                    userId: Long,
                    membershipType: Int,
                    courtNumber: Int,
                    startTime: String,
                    endTime: String): (Boolean, Boolean) = {
    val startTimeFormatted = convertToLocalTime(startTime)
    val endTimeFormatted = convertToLocalTime(endTime)
    val isAvailable = isSlotAvailable(allBookingsOnDate.filter(_.courtNumber == courtNumber), startTimeFormatted,endTimeFormatted)
    val isPermitted = isBookingPermitted(allBookingsOnDate.filter(_.userId == userId), membershipType, startTimeFormatted.until(endTimeFormatted, MINUTES))
    (isAvailable, isPermitted)
  }




  private def isSlotAvailable(bookings: Seq[Booking], proposedStartTime: LocalTime, proposedEndTime: LocalTime): Boolean = {
    bookings.forall { booking =>
      val currentStartTime = convertToLocalTime(booking.startTime)
      val currentEndTime = convertToLocalTime(booking.endTime)

      isAvailable(proposedStartTime, proposedEndTime, currentStartTime, currentEndTime)
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

  private def isBookingPermitted(bookings: Seq[Booking], membershipType: Int, bookingLength: Long): Boolean = {
    val minutesCurrentlyBooked = bookings.map { booking =>
      val startTime = convertToLocalTime(booking.startTime)
      val endTime = convertToLocalTime(booking.endTime)
      startTime.until(endTime,MINUTES)
    }.sum
    (bookingLength + minutesCurrentlyBooked) <= getMinutesPermittedPerDay(membershipType)
  }

  private def handleBookingAuthorizedResponse(isSlotAvailable: Boolean,
                             isUserAuthorised: Boolean,
                             booking: Booking,
                             token: String): Future[BookingRequestResult] = {
    log.info(s"Slot Available: $isSlotAvailable, User is Authorised: $isUserAuthorised")

    (isSlotAvailable, isUserAuthorised) match {
    case (true, true) =>
      bookingDB.insert(booking).map( booking => BookingSuccessful(booking.id.get,token))
    case (false, _) =>
      Future.successful(SlotNotAvailable(token))
    case (_, false) => Future.successful(UserHasExceededAllowedTime(token))
    }
  }

  private def perms(id: Long, courtNumber: Int, slotAvailable: Boolean): Future[BookingRequestResult] = {
    log.info(s"SLOT: $slotAvailable")
    if (slotAvailable) {
      bookingDB.update(id, courtNumber).map(_ => UpdateSuccessful("") )
    } else Future.successful(SlotNotAvailable(""))
  }

  private def handleCancellationRequest(bookingOption: Option[Booking], id: Long, token: String): Future[BookingRequestResult] = bookingOption match {
    case Some(_) =>
      bookingDB.delete(id).map(_ => CancellationSuccessful(token))
    case None =>
      log.info(s"Booking: $id does not exist")
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
