package core

import akka.actor.{ActorRef, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.ExecutionContext

trait BaseRoute {
  implicit val actorSystem: ActorSystem
  implicit val timeout: Timeout
  implicit val materializer: ActorMaterializer

  implicit def executor: ExecutionContext


  //def handleActorResponse[T](dto:T, actorRef: ActorRef): Route
  val logger: LoggingAdapter
}
