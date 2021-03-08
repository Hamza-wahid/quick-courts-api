package bookings

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe

import scala.concurrent.ExecutionContext

class BookingManager(bookingDB: BookingRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging  {

  override def receive: Receive = {

    case GetDailyBookings(DailyBookingsRequest(year, month, day)) =>
      log.info(s"Requesting bookings for the day: $day/$month/$year")
      bookingDB.getDailyBookings(year, month, day).mapTo[Seq[Booking]].pipeTo(sender())
  }
}
