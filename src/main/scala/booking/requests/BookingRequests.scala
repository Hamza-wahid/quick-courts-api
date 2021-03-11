package booking.requests

object BookingRequests {

  case class BookingsByDateRequest(year: Int, day: Int, month: Int)

  case class CreateBookingRequest(courtNumber: Int,
                                  day: Int,
                                  year: Int,
                                  month: Int,
                                  startTime: String,
                                  endTime: String)  // HH::MM format
}
