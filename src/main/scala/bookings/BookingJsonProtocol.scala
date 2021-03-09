package bookings

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import core.authorisation.Claims
import spray.json._

trait BookingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookingsFormat = jsonFormat8(Booking)
  implicit val dailyBookingsRequestFormat = jsonFormat3(DailyBookingsRequest)
  implicit val createBookingRequestFormat = jsonFormat6(CreateBookingRequest)
  implicit val createBookingFormat = jsonFormat3(CreateBooking)
  implicit val getDailyBookingFormat = jsonFormat1(GetDailyBookings)


}



