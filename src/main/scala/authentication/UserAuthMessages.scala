package authentication

trait AuthRequest

case class UserRegistrationRequest(email: String,
                                   password: String,
                                   firstName: String,
                                   lastName: String,
                                   gender: Int,
                                   membershipType: Int
                                  ) extends AuthRequest

case class UserLoginRequest(email: String, password: String) extends AuthRequest



case class RegisterUser(userRegistrationRequest: UserRegistrationRequest)
case class LoginUser(userLoginRequest: UserLoginRequest) extends AuthRequest

trait UserAuthResult

object UserAuthResult {
  case object InvalidData extends UserAuthResult

  case object UserExists extends UserAuthResult

  case object UserNonExistent extends UserAuthResult

  case class Successful(token: String) extends UserAuthResult
}