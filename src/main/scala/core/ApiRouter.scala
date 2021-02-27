package core

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import authentication.AuthenticationRouter

trait ApiRouter extends AuthenticationRouter {
  val apiRoutes: Route =
    pathPrefix("api") {
      authenticationRoutes
    }

}
