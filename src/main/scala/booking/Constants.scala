package booking

object Constants {

  // GET BOOKING(s)
  val GetBookingsLog: String => String = date => s"Retrieving bookings for the day: $date"
  val GetBookingLog: Long => String = id => s"Retrieving booking with id: $id"

  // CREATE BOOKING
  val CreateBookingLog: (Long, String, String, String) => String = (userId, date, startTime, endTime) => s"Request by user: $userId to book on the date $date between $startTime and $endTime "
  val SlotNotAvailableLog = "Proposed slot has already been taken and is not available"
  val AddingBookingLog = "Adding new booking to DB"
  val UserExceededTimeLog = "User has exceeded their daily permitted minutes"
  val SlotEligibilityLog: (Boolean, Boolean) => String = (isSlotAvailable,isUserAuthorised) => s"Slot Available: $isSlotAvailable, User is Authorised: $isUserAuthorised"

  // CANCEL BOOKING
  val CancelBookingLog: (Long, Long) => String = (userId, id) => s"Request by $userId to cancel booking: $id"
  val CancellationNotAuthorizedLog: (Long, Long, Long) => String = (currentUserId, newUserId, id) => s"User $newUserId is not authorised to cancel booking $id made by user $currentUserId"
  val DeletingBookingLog: Long => String = id => s"Deleting booking $id from DB"
  val BookingDoesNotExistLog: Long => String = id => s"Booking with id $id does not exist"

  // MODIFY BOOKING
  val ModifyBookingLog: (Long, Int) => String = (id, newCourtNumber) => s"Request to change court number of booking $id to $newCourtNumber"
  val UpdatingBookingLog: Long => String = id => s"Updating booking $id in DB"
  val UpdateNotAuthorizedLog: (Long, Long, Long) => String = (currentUserId, newUserId, id) => s"User $newUserId is not able to modify booking $id made by $currentUserId"
  val BookingExistsLog: Long => String = id => s"Booking with id $id exists"

}
