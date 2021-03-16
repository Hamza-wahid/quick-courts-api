package booking

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BookingRepo(implicit db: Database, ec: ExecutionContext) {
  private val bookingsTQ = TableQuery[Bookings]


  def getByDate(date: String): Future[Seq[Booking]] = {
    db.run(byDailyBookingsQuery(date).result)
  }

  def getByUserId(userId: Long): Future[Seq[Booking]] =
    db.run(bookingsTQ.filter(_.userId === userId).result)

  def getByDateAndCourtNumber(date: String, courtNumber: Int): Future[Seq[Booking]] =
    db.run(bookingsTQ.filter(booking => booking.date === date && booking.courtNumber === courtNumber).result)


  def getByBookingId(id: Long): Future[Option[Booking]] = {
    db.run(byBookingIdQueryCompiled(id).result.headOption)
  }

  def insert(booking: Booking): Future[Booking] = {
    val bookingWithId = (bookingsTQ returning bookingsTQ.map(_.id)
      into ((_,id) => booking.copy(id = Some(id)))) += booking

    db.run(bookingWithId)
  }

  def delete(id: Long): Future[Unit] = {
    db.run(byBookingIdQueryCompiled(id).delete).map(_ => ())
  }

  def update(id: Long, courtNumber: Int): Future[Unit] = {
    val updateQuery = bookingsTQ.filter(_.id === id).map(_.courtNumber).update(courtNumber).map(_ => ())
    db.run(updateQuery)
  }

  private def byDailyBookingsQuery(date: String) =
    bookingsTQ.filter(_.date === date)


  private def byBookingIdQuery(id: Rep[Long]) = bookingsTQ.filter(_.id === id)


  private val byBookingIdQueryCompiled = Compiled(byBookingIdQuery _)


}
