package authentication

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import authentication.AuthUtils._
import authentication.UserAuthResult._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import core.JwtAuthorisation.generateToken

class UserAuthManager(userDB: UserRepo) extends Actor with ActorLogging {

  implicit val timeout: Timeout = Timeout(2 seconds)
  implicit val executionContext: ExecutionContext = context.dispatcher
  protected val dbActor = context.actorOf(Props(new UserDBActor(userDB)))


  override def receive: Receive = {
    case userDetails @ UserRegistrationDetails(email, password, _, _, _, _) =>
      if (!isDataValid(email, password)) {
        log.warning(s"$email is not in a valid email format")
        sender() ! InvalidData
      } else {
        log.info(s"Asking db actor")
        (dbActor ? userDetails.copy(password = hashPassword(password)))
          .mapTo[UserAuthResult]
          .pipeTo(sender())
      }

    case request @ UserLoginRequest(_, _) =>
      log.info(s"Sending details to db actor for Registration")
      (dbActor ? request).mapTo[UserAuthResult].pipeTo(sender())
  }

}



