package backend.storage

import backend.configs.Configuration
import common.rich.RichFuture._
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object SlickStorageUtils
    extends ToFunctorOps with FutureInstances {
  private def toBoolean(f: Future[_])(implicit ec: ExecutionContext): Future[Boolean] = f >| true orElse false
  def apply(implicit c: Configuration): c.profile.api.TableQuery[_ <: c.profile.api.Table[_]] => StorageUtils = {
    import c.profile.api._
    val db = c.db
    table =>
      new StorageUtils {
        override def createTable(): Future[Boolean] =
          toBoolean(db run table.schema.create)
        override def clearTable(): Future[Boolean] =
          toBoolean(db run table.delete)
        override def dropTable(): Future[Boolean] =
          toBoolean(db run table.schema.drop)
        override def doesTableExist: Future[Boolean] =
          db run MTable.getTables map (tables => tables.exists(_.name.name == table.baseTableRow.tableName))
      }
  }
}
