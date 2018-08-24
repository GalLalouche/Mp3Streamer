package backend.storage

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait DbProvider extends ExecutionContext {
  //noinspection AbstractValueInTrait
  // This have to be val so it could be used as type literal.
  implicit val profile: JdbcProfile
  implicit def db: slick.jdbc.JdbcBackend#DatabaseDef
}
