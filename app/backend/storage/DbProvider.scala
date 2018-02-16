package backend.storage

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait DbProvider extends ExecutionContext {
  implicit val profile: JdbcProfile
  implicit val db: slick.jdbc.JdbcBackend#DatabaseDef
}
