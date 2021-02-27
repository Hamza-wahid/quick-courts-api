package authentication

import akka.actor.{ActorRef}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import core.BaseRoute
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import authentication.UserAuthResult.{InvalidData, Success, UserExists, UserNotExists}

trait AuthenticationRouter extends BaseRoute with UserAuthJsonProtocol {

  def userRegistrationService: ActorRef
  def userLoginService: ActorRef


  val authenticationRoutes: Route = {
    pathPrefix("auth") {
      (path("register") & pathEnd & post & entity(as[UserRegistrationDetails])) { userRegistrationDetails =>
        (userRegistrationService ? userRegistrationDetails).mapTo[UserAuthResult] match {
          case InvalidData(_) => complete(StatusCodes.BadRequest)
          case UserExists(_) => complete(StatusCodes.Conflict)
          case Success(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Created))
        }
      } ~ (path("login") & pathEnd & post & entity(as[UserLoginRequest])) { userLoginDetails =>
        (userLoginService ? userLoginDetails).mapTo[UserAuthResult] match {
          case InvalidData(_) => complete(StatusCodes.BadRequest)
          case UserNotExists(_) => complete(StatusCodes.Unauthorized)
          case Success(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.OK))
        }
      }
    }
  }



}
