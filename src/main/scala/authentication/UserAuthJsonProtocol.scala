package authentication

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait UserAuthJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userRegistrationRequestFormat = jsonFormat6(UserRegistrationRequest)
  implicit val userLoginRequestFormat = jsonFormat2(UserLoginRequest)
  implicit val registerUserFormat = jsonFormat1(RegisterUser)
  implicit val loginUserFormat = jsonFormat1(LoginUser)

}
