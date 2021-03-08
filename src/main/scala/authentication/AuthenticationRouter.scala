package authentication

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import core.BaseRoute
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import authentication.UserAuthResult.{InvalidData, Successful, UserExists, UserNonExistent}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

trait AuthenticationRouter extends BaseRoute with UserAuthJsonProtocol {

  def userAuthManger: ActorRef

  val authenticationRoutes: Route = {
    pathPrefix("auth") {
      (path("register") & pathEnd & post & entity(as[UserRegistrationRequest])) { userRegistrationRequest =>
        onSuccess((userAuthManger ? RegisterUser(userRegistrationRequest)).mapTo[UserAuthResult]) (matchUserAuthResult(_))
      } ~ (path("login") & pathEnd & post & entity(as[UserLoginRequest])) { userLoginRequest =>
        onSuccess((userAuthManger ? LoginUser(userLoginRequest)).mapTo[UserAuthResult])(matchUserAuthResult(_))
      }
    }
  }



  private def matchUserAuthResult(authResult: UserAuthResult): Route = authResult match {
    case InvalidData => complete(StatusCodes.BadRequest)
    case UserExists => complete(StatusCodes.Conflict)
    case UserNonExistent => complete(StatusCodes.Unauthorized)
    case Successful(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Created))
  }




}
