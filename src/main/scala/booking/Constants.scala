package booking

object Constants {


  val RetrievingBookings: (Int, Int, Int) => String = (day, month, year) => s"Retrieving bookings for the day: $day/$month/$year"
  val RetrievingBooking: (Long) => String = id => s"Retrieving booking with id: $id"

}
