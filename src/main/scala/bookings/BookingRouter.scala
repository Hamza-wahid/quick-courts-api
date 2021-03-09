package bookings

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import core.BaseRoute
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import bookings.BookingRequestResult.{BookingSuccessful, SlotNotAvailable, UserHasExceededAllowedTime}
import core.authorisation.JwtAuthUtils.{getTokenClaims, isTokenApproved}

trait BookingRouter extends BaseRoute with BookingJsonProtocol {


  def bookingManagerService: ActorRef

  val bookingRouter: Route = {
    (pathPrefix("booking") & optionalHeaderValueByName("Authorization"))  { token =>
      if (!isTokenApproved(token)) complete(StatusCodes.Unauthorized)
      else {
        val claims = getTokenClaims(token.get)
        post {
          (path("daily") & entity(as[DailyBookingsRequest])) { dailyBookingsRequest =>
            complete((bookingManagerService ? GetDailyBookings(dailyBookingsRequest)).mapTo[Seq[Booking]])
          } ~ (path("create") & entity(as[CreateBookingRequest])) {createBookingRequest =>
            onSuccess((bookingManagerService ? CreateBooking(claims.id, claims.membership, createBookingRequest))
              .mapTo[BookingRequestResult])(matchBookingRequestResult(_))
        }
      }
    }
  }
}

  private def matchBookingRequestResult(bookingRequestResult: BookingRequestResult): Route = bookingRequestResult match {
    case BookingSuccessful(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Created))
    case SlotNotAvailable(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Conflict))
    case UserHasExceededAllowedTime(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Unauthorized))
  }
}
