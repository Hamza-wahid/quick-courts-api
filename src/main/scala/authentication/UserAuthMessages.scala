package authentication

case class UserRegistrationDetails(email: String,
                                   password: String,
                                   firstName: String,
                                   lastName: String,
                                   gender: Int,
                                   membershipType: String
                                  )

case class UserLoginRequest(email: String, password: String)

case class ErrorWrapper(code: String,
                        userMessage: String,
                        exceptionMessage:
                        Option[String] = None)

case class StatusWrapper(status: String = "OK",
                         token:
                         Option[String] = None)




trait UserAuthResult

object UserAuthResult {
  case class InvalidData(msg: String) extends UserAuthResult

  case class UserExists(msg: String) extends UserAuthResult

  case class UserNotExists(msg: String) extends UserAuthResult

  case class Success(token: String) extends UserAuthResult
}