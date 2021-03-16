package authentication

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}

class UserRepo(implicit db: Database, ec: ExecutionContext) {
  private val userAuthTQ = TableQuery[Users]

  def getByUserEmail(email: String): Future[Option[User]] = {
    db.run((byUserEmailQueryCompiled(email).result.headOption))
  }

  def getByUserId(id: Long): Future[Option[User]] = {
    db.run((byUserIdlQueryCompiled(id).result.headOption))
  }
  def insert(user: User): Future[User] = {
    val userWithId = (userAuthTQ returning userAuthTQ.map(_.id)
      into ((_,id) => user.copy(id = Some(id)))) += user
    db.run(userWithId)
  }

  private def byUserEmailQuery(email: Rep[String]) = userAuthTQ.filter(_.email === email)
  private def byUserIdQuery(id: Rep[Long]) = userAuthTQ.filter(_.id === id)

  private val byUserEmailQueryCompiled = Compiled(byUserEmailQuery(_))
  private val byUserIdlQueryCompiled = Compiled(byUserIdQuery(_))


}