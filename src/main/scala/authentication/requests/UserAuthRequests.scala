package authentication.requests

object UserAuthRequests {
  case class UserRegistrationRequest(email: String,
                                     password: String,
                                     firstName: String,
                                     lastName: String,
                                     gender: Int,
                                     membershipType: Int
                                    )

  case class UserLoginRequest(email: String, password: String)

}
