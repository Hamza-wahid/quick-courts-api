package authentication

object Constants {

  // LOGIN
  val LoginRequestLog: String => String = email => s"$email requesting to login"
  val UserNotRegisteredLog: String => String = email => s"Login Failed: User with email $email has not yet registered"
  val IncorrectPasswordLog: String => String = email => s"Login Failed: Incorrect password provided for User: $email"
  val WritingUserLog: String => String = email => s"Inserting to new user: $email to user DB"
  val GeneratingTokenLog: String => String = email => s"Generating auth token for user: $email"

  // REGISTRATION
  val RegistrationRequestLog: String => String = email => s"$email requesting to register"
  val UserAlreadyExistsLog: String => String = email => s"Registration Failed: User with email: $email already exists"
  val InvalidCredentialsFormatLog: (String, String) => String = (email, password) => s"Registration Failed: $email or $password are not in a valid format"




}
