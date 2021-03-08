package bookings


case class GetDailyBookings(dailyBookingsRequest: DailyBookingsRequest)
case class DailyBookingsRequest(year: Int, day: Int, month: Int)

//case class MakeBooking()