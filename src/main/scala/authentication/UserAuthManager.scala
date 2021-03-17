package authentication

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import authentication.AuthUtils.{checkPassword, hashPassword, isDataValid}
import authentication.Constants._
import authentication.messages.AuthManagerMessages.{LoginUser, RegisterUser, UserAuthResult}
import authentication.messages.AuthManagerMessages.UserAuthResult._
import authentication.requests.UserAuthRequests._
import core.authorisation.JwtAuth.generateToken

import scala.concurrent.{ExecutionContext, Future}

class UserAuthManager(userDB: UserRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case RegisterUser(UserRegistrationRequest(email, password, firstName, lastName, gender, membershipType)) =>
      log.info(RegistrationRequestLog(email))
      if (!isDataValid(email, password)) {
        log.warning(InvalidCredentialsFormatLog(email, password))
        sender() ! InvalidData
      } else {
        val newUser = User(None, email, hashPassword(password), firstName, lastName, gender, membershipType)
        val userRegistrationResultFuture = for {
          existingUser <- userDB.getByUserEmail(email)
          userRegistrationResult <- handleUserRegistrationQueryResponse(existingUser, newUser)
        } yield userRegistrationResult

        userRegistrationResultFuture
          .mapTo[UserAuthResult]
          .pipeTo(sender())
      }

    case LoginUser(UserLoginRequest(email, password)) =>
      log.info(LoginRequestLog(email))
      userDB.getByUserEmail(email)
        .map(handleLoginQueryResponse(_,email,password))
        .mapTo[UserAuthResult]
        .pipeTo(sender())
  }

  private def handleUserRegistrationQueryResponse(existingUser: Option[User], newUser: User): Future[UserAuthResult] = existingUser match {
    case Some(existingUser) =>
      log.warning(UserAlreadyExistsLog(existingUser.email))
      Future.successful(UserExists)
    case None =>
      log.info(WritingUserLog(newUser.email))
      userDB.insert(newUser).map(registeredUser => Successful(generateToken(registeredUser.id.get, registeredUser.membershipType)))
  }

  private def handleLoginQueryResponse(userOption: Option[User], email: String, password: String): UserAuthResult = userOption match {
    case Some(user) if checkPassword(password, user.password) =>
      log.info(GeneratingTokenLog(email))
      Successful(generateToken(user.id.get, user.membershipType))
    case Some(_) =>
      log.warning(IncorrectPasswordLog(email))
      InvalidData
    case None =>
      log.warning(UserNotRegisteredLog(email))
      UserNonExistent
  }



}