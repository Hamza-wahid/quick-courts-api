package booking

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import booking.messages.BookingManagerMessages._
import booking.requests.BookingRequests._
import spray.json._

trait BookingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val bookingsFormat = jsonFormat6(Booking)
  implicit val dailyBookingsRequestFormat = jsonFormat1(BookingsByDateRequest)
  implicit val createBookingRequestFormat = jsonFormat3(CreateBookingRequest)
  implicit val createBookingFormat = jsonFormat3(CreateBooking)
  implicit val getDailyBookingFormat = jsonFormat1(GetBookingsByDate)
  implicit val bookingIdFormat = jsonFormat1(BookingId)
  implicit val bookingResponseFuture = jsonFormat4(BookingResponse)
}



