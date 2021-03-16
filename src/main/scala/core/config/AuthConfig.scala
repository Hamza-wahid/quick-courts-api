package core.config

import com.typesafe.config.Config

trait AuthConfig {

  def externalConfig: Config

  lazy val secretKey = externalConfig.getString("jwt.key")

}
