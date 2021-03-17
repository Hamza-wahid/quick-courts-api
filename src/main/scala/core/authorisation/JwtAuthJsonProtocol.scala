package core.authorisation
import spray.json.DefaultJsonProtocol

trait JwtAuthJsonProtocol extends DefaultJsonProtocol  {
  implicit val jwtClaimsFormat = jsonFormat2(CustomClaims)
}
