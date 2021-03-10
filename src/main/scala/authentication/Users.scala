package authentication

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

case class User(id: Option[Long],
                email: String,
                password: String,
                firstName: String,
                lastName: String,
                gender: Int,
                membershipType: Int)

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def email: Rep[String] = column[String]("email")
  def password: Rep[String] = column[String]("password")
  def firstName: Rep[String] = column[String]("first_name")
  def lastName: Rep[String] = column[String]("last_name")
  def membershipType: Rep[Int] = column[Int]("membership_type")
  def gender: Rep[Int] = column[Int]("gender")

  def * : ProvenShape[User] = (id.?, email, password, firstName, lastName, gender, membershipType) <>(User.tupled, User.unapply)
}