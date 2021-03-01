package authentication

import akka.actor.{Actor, ActorLogging}

class UserLoginService extends Actor with ActorLogging  {
  override def receive: Receive = {
    case UserLoginRequest(email, password) =>

  }
}
