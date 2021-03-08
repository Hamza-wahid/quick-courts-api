package authentication

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import authentication.AuthUtils.{checkPassword, hashPassword, isDataValid}
import authentication.UserAuthResult.{InvalidData, Successful, UserExists, UserNonExistent}
import core.authorisation.JwtAuthUtils.generateToken

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext

class UserAuthManager(userDB: UserRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case RegisterUser(UserRegistrationRequest(email, password, firstName, lastName, gender, membershipType)) =>
      val origSender = sender()
      if (!isDataValid(email, password)) {
        log.warning(s"$email is not in a valid email format")
        origSender ! InvalidData
      } else {
        userDB.findByUserEmail(email).map {
          case Some(user) =>
            log.warning(s"User with email: ${user.email} is already registered")
            origSender ! UserExists
          case None =>
            log.info(s"Inserting new user with email: $email to user database")
            userDB.insert(User(None, email, hashPassword(password), firstName, lastName,gender, membershipType)).map {x =>
              origSender ! Successful(generateToken(x.id.get, x.membershipType, 5))
            }
        }
      }

    case LoginUser(UserLoginRequest(email, password)) =>
      userDB.findByUserEmail(email).map {
        case Some(user) if checkPassword(password, user.password) =>
          log.info(s"User with email: ${user.email} exists")
          Successful(generateToken(user.id.get, user.membershipType))
        case Some(_) =>
          log.info(s"Incorrect password provided for user: $email")
          InvalidData
        case None =>
          log.info(s"User with email $email has not yet registered")
          UserNonExistent
      }.pipeTo(sender())
  }


}