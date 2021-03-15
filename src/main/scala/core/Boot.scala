package core

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import authentication.{UserAuthManager, UserRepo}
import booking.{BookingManager, BookingRepo}
import com.typesafe.config.ConfigFactory
import core.config.{DatabaseConfig, ServerConfig, AuthConfig}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


object Boot extends App with ApiRouter with DatabaseConfig with ServerConfig with AuthConfig {

  override val config = ConfigFactory.load()
  override val externalConfig = ConfigFactory.load("external.conf")

  override implicit val actorSystem = ActorSystem("rest-api-app")
  override val executor = global
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(actorSystem, getClass)
  implicit val timeout = Timeout(2 seconds)
  implicit val db = Database.forURL(dbUrl, dbUser, dbPassword)

  val userDB = new UserRepo
  val bookingDB = new BookingRepo

  override val userAuthManger = actorSystem.actorOf(Props(new UserAuthManager(userDB)))

  Http().bindAndHandle(apiRoutes, serverInterface, serverPort)

  override val bookingManagerService = actorSystem.actorOf(Props(new BookingManager(bookingDB)))
}
