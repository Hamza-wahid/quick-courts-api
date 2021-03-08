package bookings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait BookingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookingsFormat = jsonFormat8(Booking)
  implicit val dailyBookingsRequestFormat = jsonFormat3(DailyBookingsRequest)
  implicit val getDailyBookingFormat = jsonFormat1(GetDailyBookings)


}



