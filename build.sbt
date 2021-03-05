name := "wcc-tennis-api"

version := "0.1"

scalaVersion := "2.12.7"


val akkaVersion = "2.5.19"
val akkaHttpVersion = "10.1.7"
val slickV = "3.2.1"
val postgresVersion = "42.2.2"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.mindrot" % "jbcrypt" % "0.3m",

  "org.postgresql" % "postgresql" % postgresVersion,
  "com.typesafe.slick" %% "slick" % slickV,


  "com.pauldijou" %% "jwt-spray-json" % "5.0.0",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5"
)