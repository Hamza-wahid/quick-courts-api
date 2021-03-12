package booking.messages

import booking.requests.BookingRequests._


object BookingManagerMessages {
  case class CreateBooking(userId: Long,
                           membershipType: Int,
                           createBookingRequest: CreateBookingRequest
                          )

  case class CancelBooking(userId: Long,
                           membershipType: Int,
                           id: Long)


  case class GetAllMemberBookings(userId: Long)
  case class GetBookingsByDate(dailyBookingsRequest: BookingsByDateRequest)
  case class GetBooking(id: Long)

  case class BookingId(bookingId: Long)

  trait BookingRequestResult

  object BookingRequestResult {

    case class BookingSuccessful(bookingId: Long, token: String) extends BookingRequestResult

    case class SlotNotAvailable(token: String) extends BookingRequestResult

    case class UserHasExceededAllowedTime(token: String) extends BookingRequestResult

    case class CancellationSuccessful(token: String) extends BookingRequestResult

    case class BookingDoesNotExist(token: String) extends BookingRequestResult

  }
}


