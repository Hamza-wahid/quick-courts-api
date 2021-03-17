package core.authorisation

import java.time.{Instant, LocalTime}

import com.typesafe.config.ConfigFactory
import core.Boot.secretKey
import core.Constants.Issuer
import core.authorisation.MembershipPrivileges.membershipPrivilegesMap
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import scala.util.{Failure, Success}
import spray.json._

case class CustomClaims(id: Long, membership: Int)

object JwtAuth extends JwtAuthJsonProtocol  {


  val algorithm = JwtAlgorithm.HS256
  val key = secretKey

  def generateToken(id: Long, membership: Int, expirationTimeSeconds: Int = 600): String = {
    val claims = JwtClaim (
      expiration = Some(Instant.now.plusSeconds(expirationTimeSeconds).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some(Issuer),
      content = CustomClaims(id, membership).toJson.prettyPrint
    )
    JwtSprayJson.encode(claims, key, algorithm)
  }


  def getTokenClaims(token: String): CustomClaims = {
    JwtSprayJson.decode(token, key, Seq(algorithm)) match {
      case Success(claims) => claims.content.parseJson.convertTo[CustomClaims]
    }
  }

  def isTokenApproved(tokenOption: Option[String]): Boolean = tokenOption match {
    case None => false
    case Some(token) if isTokenExpired(token) || !isTokenValid(token) =>
      println(!isTokenValid(token))
      println(isTokenExpired(token))
      false
    case _ => true
  }

  def getMinutesPermittedPerDay(membershipType: Int): Int = membershipPrivilegesMap(membershipType)

  private def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, key, Seq(algorithm)) match {
    case Success(claims) =>
      val a = claims.expiration.getOrElse(0.asInstanceOf[Long])
      a < Instant.now.getEpochSecond
    case Failure(_) =>
      true
  }

  private def isTokenValid(token: String):Boolean = JwtSprayJson.isValid(token , key, Seq(algorithm))

}
