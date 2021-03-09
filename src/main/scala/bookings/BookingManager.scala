package bookings

import java.time.LocalTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import bookings.BookingRequestResult.{BookingSuccessful, SlotNotAvailable, UserHasExceededAllowedTime}
import core.authorisation.JwtAuthUtils.{generateToken, getMinutesPermittedPerDay}
import java.time.temporal.ChronoUnit.MINUTES

import scala.concurrent.ExecutionContext
import scala.util.Try

class BookingManager(bookingDB: BookingRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {

  override def receive: Receive = {

    case GetDailyBookings(DailyBookingsRequest(year, month, day)) =>
      log.info(s"Requesting bookings for the day: $day/$month/$year")
      bookingDB.getDailyBookings(year, month, day).mapTo[Seq[Booking]].pipeTo(sender())


    case CreateBooking(userId, membershipType, CreateBookingRequest(courtNumber,day,year,month,startTime,endTime)) =>
      log.info(s"Request by user: $userId to book on the date $day/$month/$year between $startTime and $endTime ")

      val origSender = sender()
      val booking = Booking(None,userId, membershipType,year, month, day, startTime, endTime)
      val token = generateToken(userId, membershipType)

       bookingDB.getDailyBookings(year, month, day).map{allBookingsOnDate =>
         val (isAvailable, isPermitted) = isBookingAuthorised(allBookingsOnDate, userId, membershipType, courtNumber, startTime, endTime)
        log.info(s"Slot Availability: $isAvailable, Member is permitted: $isPermitted")
        handleSlotAvailabilityResponse(isAvailable, isPermitted, booking, origSender, token)
      }



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

  def stringToLocalTime(dateString: String): LocalTime = LocalTime.parse(dateString)


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
                             origSender: ActorRef,
                             token: String): Unit = (isSlotAvailable, isUserAuthorised) match {
    case (true, true) =>
      bookingDB.insert(booking).map(_ => origSender ! BookingSuccessful(token))
    case (false, _) => origSender ! SlotNotAvailable(token)
    case (_, false) => origSender ! UserHasExceededAllowedTime(token)
  }
}
