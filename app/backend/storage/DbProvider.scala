package backend.storage

import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait DbProvider {
  //noinspection AbstractValueInTrait
  // This have to be val so it could be used as type literal.
  val profile: JdbcProfile
  def db: slick.jdbc.JdbcBackend#DatabaseDef
  // Some databases require unique constraint names.
  def constraintMangler(name: String): String
}
