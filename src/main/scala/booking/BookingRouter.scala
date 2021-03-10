package booking

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import core.BaseRoute
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import booking.messages.BookingManagerMessages.BookingRequestResult._
import booking.messages.BookingManagerMessages._
import booking.requests.BookingRequests._
import core.authorisation.JwtAuthUtils.{getTokenClaims, isTokenApproved}


trait BookingRouter extends BaseRoute with BookingJsonProtocol {


  def bookingManagerService: ActorRef

  val bookingRouter: Route = {
    (pathPrefix("booking") & optionalHeaderValueByName("Authorization"))  { token =>
      if (!isTokenApproved(token)) complete(StatusCodes.Unauthorized)
      else {
        val claims = getTokenClaims(token.get)
        post {
          (path("date") & entity(as[BookingsByDateRequest])) { bookingsByDateRequest =>
            complete((bookingManagerService ? GetBookingsByDate(bookingsByDateRequest))
              .mapTo[Seq[Booking]])
          } ~ (path("create") & entity(as[CreateBookingRequest])) {createBookingRequest =>
            onSuccess((bookingManagerService ? CreateBooking(claims.id, claims.membership, createBookingRequest))
              .mapTo[BookingRequestResult])(matchBookingRequestResult)
        }
      } ~ get {
          path(LongNumber) { userId =>
            complete((bookingManagerService ? GetAllMemberBookings(userId))
              .mapTo[Seq[Booking]])
          }
        } ~(path(LongNumber / "cancel") & delete) { bookingId =>
          onSuccess((bookingManagerService ? CancelBooking(claims.id, claims.membership, bookingId))
            .mapTo[BookingRequestResult])(matchBookingRequestResult)
        }
    }
  }
}

  private def matchBookingRequestResult(bookingRequestResult: BookingRequestResult): Route = bookingRequestResult match {
    case BookingSuccessful(bookingId,token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Created -> BookId(bookingId)))
    case SlotNotAvailable(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Conflict))
    case UserHasExceededAllowedTime(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.Unauthorized))
    case CancellationSuccessful(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.OK))
    case BookingDoesNotExist(token) => respondWithHeader(RawHeader("Access-Token", token))(complete(StatusCodes.NotFound))
  }
}
