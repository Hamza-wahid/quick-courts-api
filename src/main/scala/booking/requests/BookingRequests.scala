package booking.requests

object BookingRequests {

  case class BookingsByDateRequest(date: String)

  case class CreateBookingRequest(courtNumber: Int,
                                  startDateTime: String,
                                  endDateTime: String)
}
