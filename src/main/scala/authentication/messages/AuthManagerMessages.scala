package authentication.messages

import authentication.requests.UserAuthRequests._

object AuthManagerMessages {
  case class RegisterUser(userRegistrationRequest: UserRegistrationRequest)
  case class LoginUser(userLoginRequest: UserLoginRequest)

  trait UserAuthResult

  object UserAuthResult {
    case object InvalidData extends UserAuthResult

    case object UserExists extends UserAuthResult

    case object UserNonExistent extends UserAuthResult

    case class Successful(token: String) extends UserAuthResult
  }
}








