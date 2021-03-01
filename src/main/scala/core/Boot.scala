package core

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.util.Timeout
import authentication.{UserLoginService, UserRegistrationService}

import scala.concurrent.duration.DurationInt


object Boot extends App with ApiRouter {

  override implicit val actorSystem = ActorSystem("rest-api-app")
  override implicit val executor = actorSystem.dispatcher
  override val logger = Logging(actorSystem, getClass)
  implicit val timeout = Timeout(2 seconds)

  override val userRegistrationService = actorSystem.actorOf(Props[UserRegistrationService])
  override val userLoginService = actorSystem.actorOf(Props[UserLoginService])


  Http().bindAndHandle(apiRoutes, "localhost", 8080)



}
