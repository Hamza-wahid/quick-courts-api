package authentication

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait UserAuthJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userRegistrationFormat = jsonFormat6(UserRegistrationDetails)
  implicit val userLoginRequestFormat = jsonFormat2(UserLoginRequest)

}
