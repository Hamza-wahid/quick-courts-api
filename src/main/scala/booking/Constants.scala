package booking

object Constants {


  val RetrievingBookings: (String) => String = date => s"Retrieving bookings for the day: $date"
  val RetrievingBooking: (Long) => String = id => s"Retrieving booking with id: $id"

}
