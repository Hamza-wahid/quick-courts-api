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
import core.Constants.{AccessToken, Authorization}
import core.authorisation.JwtAuthUtils.{getTokenClaims, isTokenApproved}


trait BookingRouter extends BaseRoute with BookingJsonProtocol {


  // TODO: Add an update endpoint + Read me file

  def bookingManagerService: ActorRef

  val bookingRouter: Route = {
    (pathPrefix("booking") & optionalHeaderValueByName(Authorization))  { token =>
      if (!isTokenApproved(token)) complete(StatusCodes.Unauthorized)
      else {
        val claims = getTokenClaims(token.get)
          (pathEnd & entity(as[CreateBookingRequest]) & post) {createBookingRequest =>
            onSuccess((bookingManagerService ? CreateBooking(claims.id, claims.membership, createBookingRequest))
              .mapTo[BookingRequestResult])(matchBookingRequestResult)
        } ~ get {
          (pathEnd & parameters('day.as[Int], 'month.as[Int], 'year.as[Int])) { (day, month, year) =>
            complete((bookingManagerService ? GetBookingsByDate(BookingsByDateRequest(year,day,month)))
              .mapTo[Seq[Booking]])
          } ~ path(LongNumber) { bookingId =>
            complete((bookingManagerService ? GetBooking(bookingId))
              .mapTo[Option[Booking]])
          }
        } ~ (path(LongNumber) & delete) { bookingId =>
          onSuccess((bookingManagerService ? CancelBooking(claims.id, claims.membership, bookingId))
            .mapTo[BookingRequestResult])(matchBookingRequestResult)
        }
    }
  }
}

  private def matchBookingRequestResult(bookingRequestResult: BookingRequestResult): Route = bookingRequestResult match {
    case BookingSuccessful(bookingId,token) => respondWithHeader(RawHeader(AccessToken, token))(complete(StatusCodes.Created -> BookingId(bookingId)))
    case SlotNotAvailable(token) => respondWithHeader(RawHeader(AccessToken, token))(complete(StatusCodes.Conflict))
    case UserHasExceededAllowedTime(token) => respondWithHeader(RawHeader(AccessToken, token))(complete(StatusCodes.Forbidden))
    case CancellationSuccessful(token) => respondWithHeader(RawHeader(AccessToken, token))(complete(StatusCodes.NoContent))
    case BookingDoesNotExist(token) => respondWithHeader(RawHeader(AccessToken, token))(complete(StatusCodes.NotFound))
  }
}
