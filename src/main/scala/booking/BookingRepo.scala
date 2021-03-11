package booking

import java.sql.Date
import java.time.{ LocalDate}

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BookingRepo(implicit db: Database, ec: ExecutionContext) {
  private val bookingsTQ = TableQuery[Bookings]


  def getByDate(year: Int, month: Int, day: Int): Future[Seq[Booking]] = {
    db.run(byDailyBookingsQuery(year, month, day).result)
  }

  def getByUserId(userId: Long): Future[Seq[Booking]] =
    db.run(bookingsTQ.filter(_.userId === userId).result)


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

  private def byDailyBookingsQuery(year: Int, month: Int, day: Int) =
    bookingsTQ.filter(x=> (x.day === day) && (x.year === year) && (x.month === month))


  private def byBookingIdQuery(id: Rep[Long]) = bookingsTQ.filter(_.id === id)


  private val byBookingIdQueryCompiled = Compiled(byBookingIdQuery _)


}
