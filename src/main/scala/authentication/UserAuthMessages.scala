package authentication

case class UserRegistrationDetails(email: String,
                                   password: String,
                                   firstName: String,
                                   lastName: String,
                                   gender: Int,
                                   membershipType: Int
                                  )

case class UserLoginRequest(email: String, password: String)


trait UserAuthResult

object UserAuthResult {
  case object InvalidData extends UserAuthResult

  case class UserExists(msg: String) extends UserAuthResult

  case object UserNonExistent extends UserAuthResult

  case class Success(token: String) extends UserAuthResult
}