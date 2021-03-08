package core.authorisation

import java.sql.Timestamp
import java.time.{Instant, LocalTime}

import akka.http.scaladsl.model.StatusCodes
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import slick.lifted.SimpleFunction

import scala.util.{Failure, Success}
import spray.json._
import slick.jdbc.PostgresProfile.api._

case class Claims(id: Long, membership: Int)



object JwtAuthUtils extends JwtAuthJsonProtocol {


  val algorithm = JwtAlgorithm.HS256
  val secretKey = "secret"   // TODO: Retrieve this from somewhere secure

  def generateToken(id: Long, membership: Int, expirationTimeSeconds: Int = 300): String = {
    val claims = JwtClaim (
      expiration = Some(Instant.now.plusSeconds(expirationTimeSeconds).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some("wcc.com"),
      content =
        s"""
          |{
          |"id": ${id + ","}
          |"membership": ${membership}
          |}
          |
          |""".stripMargin
    )
    JwtSprayJson.encode(claims, secretKey, algorithm)
  }

  def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) =>
      val a = claims.expiration.getOrElse(0.asInstanceOf[Long])
      a < Instant.now.getEpochSecond
    case Failure(_) =>
      println("Failure")
      true
  }

  def isTokenValid(token: String):Boolean = JwtSprayJson.isValid(token , secretKey, Seq(algorithm))

  def getTokenClaims(token: String): Option[Claims] = {
    JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
      case Success(claims) => Option(claims.content.parseJson.convertTo[Claims])
      case Failure(_) => None
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

  def isAuthorised(authorisiedMembers: Seq[Int], membershipType: Int): Boolean = authorisiedMembers.contains(membershipType)

//  val token = generateToken(1, 1, 1000000000)
//  println(token)
//
//  println(isTokenApproved(Option(token)))

}
