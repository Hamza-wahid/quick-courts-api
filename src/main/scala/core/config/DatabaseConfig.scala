package core.config

import com.typesafe.config.{Config}

trait DatabaseConfig {
  def externalConfig: Config

  lazy val dbUrl = externalConfig.getString("db.url")
  lazy val dbUser = externalConfig.getString("db.user")
  lazy val dbPassword = externalConfig.getString("db.password")
}
