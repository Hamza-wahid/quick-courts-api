package authentication

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class UserRepo(implicit db: Database, ec: ExecutionContext) {
  private val userAuthTQ = TableQuery[Users]

  def findByUserEmail(email: String): Future[Option[User]] = {
    db.run((byUserEmailQueryCompiled(email).result.headOption))
  }

  def insert(user: User): Future[User] = {
    val userWithId = (userAuthTQ returning userAuthTQ.map(_.id)
      into ((_,id) => user.copy(id = Some(id)))) += user
    db.run(userWithId)
  }

  private def byUserEmailQuery(email: Rep[String]) = userAuthTQ.filter(_.email === email)

  private val byUserEmailQueryCompiled = Compiled(byUserEmailQuery(_))

}