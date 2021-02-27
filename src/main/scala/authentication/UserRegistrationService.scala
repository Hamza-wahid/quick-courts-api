package authentication

import akka.actor.{Actor, ActorLogging}
import authentication.RegistrationDetailsValidator.isDataValid
import authentication.UserAuthResult._





class UserRegistrationService extends Actor with ActorLogging {
  override def receive: Receive = {
    case UserRegistrationDetails(email, password, firstName, lastName, gender, membershipType) =>
      if (!isDataValid(email, password)) {
        InvalidData("E-mail or password is invalid")
      } //TODO: Database logic
  }
}