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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BookingManager(bookingDB: BookingRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {


  override def receive: Receive = {



    case GetBookingsByDate(BookingsByDateRequest(year, month, day)) =>
      log.info(RetrievingBookings(day, month, year))
      bookingDB
        .getByDate(year, month, day)
        .mapTo[Seq[Booking]]
        .pipeTo(sender())


    case GetAllMemberBookings(userId) =>
      log.info(s"Retrieving the bookings for member: $userId")
      bookingDB.getByUserId(userId)
        .mapTo[Seq[Booking]]
        .pipeTo(sender())

    case GetBooking(id) =>
      log.info(RetrievingBooking(id))
      bookingDB.getByBookingId(id)
        .mapTo[Option[Booking]]
        .pipeTo(sender())


    case CreateBooking(userId, membershipType, CreateBookingRequest(courtNumber,day,year,month,startTime,endTime)) =>
      log.info(s"Request by user: $userId to book on the date $day/$month/$year between $startTime and $endTime ")

      val booking = Booking(None,userId, courtNumber,year, month, day, startTime, endTime)
      val token = generateToken(userId, membershipType)

      val bookingRequestResultFuture = for {
        allBookingsOnDate <- bookingDB.getByDate(year, month, day)
        (isAvailable, isPermitted) = isBookingAuthorised(allBookingsOnDate, userId, membershipType, courtNumber, startTime, endTime)
        bookingRequestResult <- handleSlotAvailabilityResponse(isAvailable, isPermitted, booking, token)
      } yield bookingRequestResult

      bookingRequestResultFuture.mapTo[BookingRequestResult].pipeTo(sender())

    case CancelBooking(userId, membershipType, id) =>
      log.info(s"Request by $userId to cancel booking: $id")
      val token = generateToken(userId, membershipType)

      val bookingRequestResultFuture = for {
        bookingOption <- bookingDB.getByBookingId(id)
        bookingRequestResult <- handleCancellationRequest(bookingOption,id,token)
      } yield bookingRequestResult

      bookingRequestResultFuture.mapTo[BookingRequestResult].pipeTo(sender())


  }

  private def isBookingAuthorised(allBookingsOnDate: Seq[Booking],
                    userId: Long,
                    membershipType: Int,
                    courtNumber: Int,
                    startTime: String,
                    endTime: String): (Boolean, Boolean) = {
    val startTimeFormatted = stringToLocalTime(startTime)
    val endTimeFormatted = stringToLocalTime(endTime)
    val isAvailable = isSlotAvailable(allBookingsOnDate.filter(_.courtNumber == courtNumber), startTimeFormatted,endTimeFormatted)
    val isPermitted = isBookingPermitted(allBookingsOnDate.filter(_.userId == userId), membershipType, startTimeFormatted.until(endTimeFormatted, MINUTES))
    (isAvailable, isPermitted)
  }

  private def stringToLocalTime(timeString: String): LocalTime = LocalTime.parse(timeString)


  private def isSlotAvailable(bookings: Seq[Booking], startTime: LocalTime, endTime: LocalTime): Boolean = {
    bookings.forall { booking =>
      val currentStartTime = LocalTime.parse(booking.startTime)
      val currentEndTime = LocalTime.parse(booking.endTime)

      val startTimeAvailable = !((startTime.isAfter(currentStartTime) && startTime.isBefore(currentEndTime)) || startTime == currentStartTime)  // true if available
      val endTimeAvailable = !((endTime.isAfter(currentStartTime) && endTime.isBefore(currentEndTime)) || endTime == currentEndTime)

      startTimeAvailable && endTimeAvailable
    }
  }

  private def isBookingPermitted(bookings: Seq[Booking], membershipType: Int, bookingLength: Long): Boolean = {
    val minutesCurrentlyBooked = bookings.map { booking =>
      val a = stringToLocalTime(booking.startTime)
      val b = stringToLocalTime(booking.endTime)
      a.until(b,MINUTES )
    }.sum
    (bookingLength + minutesCurrentlyBooked) <= getMinutesPermittedPerDay(membershipType)
  }

  private def handleSlotAvailabilityResponse(isSlotAvailable: Boolean,
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

  private def handleCancellationRequest(bookingOption: Option[Booking], id: Long, token: String): Future[BookingRequestResult] = bookingOption match {
    case Some(_) =>
      bookingDB.delete(id).map(_ => CancellationSuccessful(token))
    case None =>
      log.info(s"Booking: $id does not exist")
      Future.successful(BookingDoesNotExist(token))
  }
}
