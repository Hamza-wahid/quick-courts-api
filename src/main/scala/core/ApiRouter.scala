package core

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import authentication.AuthenticationRouter
import bookings.BookingRouter

trait ApiRouter extends AuthenticationRouter with BookingRouter {
  val apiRoutes: Route =
    pathPrefix("api") {
      authenticationRoutes ~ bookingRouter
    }

}
