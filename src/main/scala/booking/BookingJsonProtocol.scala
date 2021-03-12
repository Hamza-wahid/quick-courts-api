package booking

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import booking.messages.BookingManagerMessages._
import booking.requests.BookingRequests._
import spray.json._

trait BookingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookingsFormat = jsonFormat8(Booking)
  implicit val dailyBookingsRequestFormat = jsonFormat3(BookingsByDateRequest)
  implicit val createBookingRequestFormat = jsonFormat6(CreateBookingRequest)
  implicit val createBookingFormat = jsonFormat3(CreateBooking)
  implicit val getDailyBookingFormat = jsonFormat1(GetBookingsByDate)
  implicit val bookingIdFormat = jsonFormat1(BookingId)


}



