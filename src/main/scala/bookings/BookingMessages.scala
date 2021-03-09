package bookings

import core.authorisation.Claims


case class GetDailyBookings(dailyBookingsRequest: DailyBookingsRequest)
case class DailyBookingsRequest(year: Int, day: Int, month: Int)

case class CreateBookingRequest(courtNumber: Int,
                                day: Int,
                                year: Int,
                                month: Int,
                                startTime: String,
                                endTime: String)  // HH::MM format

case class CreateBooking(userId: Long,
                         membershipType: Int,
                         createBookingRequest: CreateBookingRequest
                        )

//case class MakeBooking()




trait BookingRequestResult


object BookingRequestResult {

  case class BookingSuccessful(token: String) extends BookingRequestResult
  case class SlotNotAvailable(token: String) extends BookingRequestResult
  case class UserHasExceededAllowedTime(token: String) extends BookingRequestResult
}