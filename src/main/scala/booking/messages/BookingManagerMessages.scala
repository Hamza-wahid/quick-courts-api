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
  case class GetBooking(bookingId: Long, userId: Long, membershipType: Int)

  case class ModifyBooking(bookingId: Long,
                           userId: Long, membershipType: Int,
                           newCourtNumber: Int)

  case class BookingResponse(courtNumber: Int,
                             date: String,
                             startTime: String,
                             endTime: String
                    )


  case class BookingId(bookingId: Long)

  trait BookingRequestResult

  object BookingRequestResult {

    case class BookingSuccessful(bookingId: Long, token: String) extends BookingRequestResult

    case class SlotNotAvailable(token: String) extends BookingRequestResult

    case class CancellationSuccessful(token: String) extends BookingRequestResult

    case class BookingDoesNotExist(token: String) extends BookingRequestResult

    case class BookingExists(token: String, booking: BookingResponse) extends BookingRequestResult

    case class UpdateSuccessful(token: String) extends BookingRequestResult

    case class UserNotAuthorised(token: String) extends BookingRequestResult
  }
}


