package authentication

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Route, StandardRoute}
import core.BaseRoute
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import authentication.UserAuthResult.{InvalidData, Success, UserExists, UserNonExistent}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait AuthenticationRouter extends BaseRoute with UserAuthJsonProtocol {

  def userAuthManger: ActorRef

  val authenticationRoutes: Route = {
    pathPrefix("auth") {
      (path("register") & pathEnd & post & entity(as[UserRegistrationDetails])) { userRegistrationDetails =>
        handleActorResponse(userRegistrationDetails)

      } ~ (path("login") & pathEnd & post & entity(as[UserLoginRequest])) { userLoginDetails =>
        handleActorResponse(userLoginDetails)
      }
    }
  }


  def handleActorResponse[T](dto: T, userAuthManagerRef: ActorRef = userAuthManger): Route = {
    val route = for {
      userAuthResult <- (userAuthManagerRef ? dto).mapTo[UserAuthResult]
    } yield matchUserAuthResult(userAuthResult)

    Await.result(route, 2 seconds)
  }

  private def matchUserAuthResult(authResult: UserAuthResult): Route = authResult match {
    case InvalidData => complete(StatusCodes.BadRequest)
    case UserExists(_) => complete(StatusCodes.Conflict)
    case UserNonExistent => complete(StatusCodes.Unauthorized)
    case Success(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Created))
  }




}
