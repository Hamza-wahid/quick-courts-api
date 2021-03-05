package authentication

import org.mindrot.jbcrypt.BCrypt


object AuthUtils {

  def isDataValid(email: String, password: String): Boolean = validEmail(email.trim) && validPassword(password.trim)

  def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

  def checkPassword(password: String, passwordHash: String): Boolean = BCrypt.checkpw(password, passwordHash)

  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmail(email: String) = emailRegex.findFirstMatchIn(email).isDefined

  private def validPassword(password: String): Boolean = !password.isEmpty && password.size > 7

}
