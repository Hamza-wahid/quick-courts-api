package authentication

object Constants {

  // LOGIN
  val LoginRequest: String => String = email => s"$email requesting to login"
  val UserNotRegistered: String => String = email => s"Login Failed: User with email $email has not yet registered"
  val IncorrectPassword: String => String = email => s"Login Failed: Incorrect password provided for User: $email"
  val WritingUser: String => String = email => s"Inserting to new user: $email to user DB"
  val GeneratingToken: String => String = email => s"Generating auth token for user: $email"

  // REGISTRATION
  val RegistrationRequest: String => String = email => s"$email requesting to register"
  val UserAlreadyExists: String => String = email => s"Registration Failed: User with email: $email already exists"
  val InvalidCredentialsFormat: (String, String) => String = (email, password) => s"Registration Failed: $email or $password are not in a valid format"




}
