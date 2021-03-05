package authentication

import akka.actor.{Actor, ActorLogging}
import authentication.AuthUtils.checkPassword
import authentication.UserAuthResult.{InvalidData, Success, UserExists, UserNonExistent}
import core.JwtAuthorisation.generateToken

import scala.concurrent.ExecutionContext

class UserDBActor(userDB: UserRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case UserRegistrationDetails(email, password, firstName, lastName, gender, membershipType) =>
      val origSender = sender()
      log.info("made it here")
      userDB.findByUserEmail(email).map {
        case Some(user) =>
          log.warning(s"User with email: ${user.email} is already registered")
          origSender ! UserExists("")
        case None =>
          log.info(s"Inserting new user with email: $email to user database")
          userDB.insert(User(None, email, password, firstName, lastName,gender, membershipType)).map {x =>
            origSender ! Success(generateToken(x.id.get, x.membershipType, 5))
          }.foreach(origSender ! _)
      }

    case UserLoginRequest(email, password) =>
      val origSender = sender()
      userDB.findByUserEmail(email).map {
        case Some(user) if checkPassword(password, user.password) =>
          log.info(s"User with email: ${user.email} exists")
          Success(generateToken(user.id.get, user.membershipType, 5))
        case Some(_) =>
          log.info(s"Incorrect password provided for user: $email")
          InvalidData
        case None =>
          log.info(s"User with email $email has not yet registered")
          UserNonExistent
      }.foreach(origSender ! _)
  }
}