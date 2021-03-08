package bookings

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import core.BaseRoute
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import core.authorisation.JwtAuthUtils.{getTokenClaims, isTokenApproved}

trait BookingRouter extends BaseRoute with BookingJsonProtocol {


  def bookingManagerService: ActorRef

  val bookingRouter: Route = {
    (pathPrefix("booking") & optionalHeaderValueByName("Authorization"))  { token =>
      println(token)
      if (!isTokenApproved(token)) {
        println("POSSED")
        complete(StatusCodes.Unauthorized)
      } else {
        val claims = getTokenClaims(token.get)
        post {
          (path("daily") & entity(as[DailyBookingsRequest])) { dailyBookingsRequest =>
            complete((bookingManagerService ? GetDailyBookings(dailyBookingsRequest)).mapTo[Seq[Booking]])
          }
        }
      }
    }
  }

}
