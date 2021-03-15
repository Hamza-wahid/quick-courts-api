package booking

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape


case class Booking(id: Option[Long],
                   userId: Long,
                   courtNumber: Int,
                   date: String,
                   startTime: String,
                   endTime: String,

                  )

class Bookings(tag: Tag) extends Table[Booking](tag, "bookings") {

  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[Long] = column[Long]("user_id")
  def courtNumber: Rep[Int] = column[Int]("court_number")
  def date: Rep[String] = column[String]("date")
  def startTime: Rep[String] = column[String]("start_time")
  def endTime: Rep[String] = column[String]("end_time")

  def * : ProvenShape[Booking] = (id.?, userId, courtNumber, date, startTime, endTime) <>(Booking.tupled, Booking.unapply)
}