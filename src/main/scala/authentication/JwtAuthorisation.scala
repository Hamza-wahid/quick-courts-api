//package authentication
//
//import java.util.concurrent.TimeUnit
//
//import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
//
//import scala.util.{Failure, Success}
//
//
////TODO
//trait JwtAuthorisation {
//
//  val permissions  = Map(
//    0 -> "PLATINUM",
//    1 -> "GOLD",
//    2 -> "SILVER"
//  )
//
//  def membershipType(membershipCategory: Int): String = permissions(membershipCategory)
//
//  val algorithm = JwtAlgorithm.HS256
//  val secretKey = "secret"   // TODO: Retrieve this from somewhere secure
//
//  def generateToken(username: String, expirationTimeMinutes: Int): String = {
//    val claims = JwtClaim (
//      expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.MINUTES.toSeconds(expirationTimeMinutes)),
//      issuedAt = Some(System.currentTimeMillis() / 1000),
//      issuer = Some("wcc.com") //TODO: Add content field with membership value
//    )
//
//    JwtSprayJson.encode(claims, secretKey, algorithm)
//  }
//
//  def isTokenExpired(token: String) = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
//    case Success(claims) => claims.expiration.getOrElse(0) < System.currentTimeMillis() / 1000
//    case Failure(_) => true
//  }
//
//  def isTokenValid(token: String):Boolean = JwtSprayJson.isValid(token , secretKey, Seq(algorithm))
//}
