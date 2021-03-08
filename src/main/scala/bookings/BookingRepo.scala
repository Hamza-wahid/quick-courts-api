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

  private def byDailyBookingsQuery(year: Int, month: Int, day: Int) =
    bookingsTQ.filter(x=> (x.day === day) && (x.year === year) && (x.month === month))





}
