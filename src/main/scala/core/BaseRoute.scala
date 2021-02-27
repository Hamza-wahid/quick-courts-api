package core

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.util.Timeout
import scala.concurrent.duration.DurationInt


import scala.concurrent.ExecutionContext

trait BaseRoute {
  implicit val actorSystem: ActorSystem
  implicit val timeout: Timeout

  implicit def executor: ExecutionContext

  val logger: LoggingAdapter
}
