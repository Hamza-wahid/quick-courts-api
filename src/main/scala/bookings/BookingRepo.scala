package bookings

import java.sql.Date
import java.time.{ LocalDate}

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class BookingRepo(implicit db: Database, ec: ExecutionContext) {
  private val bookingsTQ = TableQuery[Bookings]


  def getDailyBookings(year: Int, month: Int, day: Int): Future[Seq[Booking]] = {
    val query = byDailyBookingsQuery(year, month, day)
    db.run(query.result)
  }

  def getUserBookings(id: Long, year: Int, month: Int, day: Int) = {
    val a = bookingsTQ.filter {x =>
      x.id === id && x.day === day && x.year === year && x.month === month
    }
    db.run(a.result)
  }

  def insert(booking: Booking): Future[Booking] = {
    val bookingWithId = (bookingsTQ returning bookingsTQ.map(_.id)
      into ((_,id) => booking.copy(id = Some(id)))) += booking

    db.run(bookingWithId)
  }

  private def byDailyBookingsQuery(year: Int, month: Int, day: Int) =
    bookingsTQ.filter(x=> (x.day === day) && (x.year === year) && (x.month === month))




}
