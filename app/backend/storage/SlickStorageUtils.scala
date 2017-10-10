package backend.storage

import backend.configs.Configuration
import common.rich.RichFuture._
import common.storage.{TableUtils, TableUtilsTemplate}
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object SlickStorageUtils
    extends ToFunctorOps with FutureInstances {
  def apply(implicit c: Configuration): c.profile.api.TableQuery[_ <: c.profile.api.Table[_]] => TableUtils = {
    import c.profile.api._
    val db = c.db
    table =>
      new TableUtilsTemplate() {
        override def createTable(): Future[_] = db run table.schema.create
        override protected def forceDropTable() = db run table.schema.drop
        override def clearTable(): Future[_] = db run table.delete
        override def doesTableExist: Future[Boolean] =
          db run MTable.getTables map (tables => tables.exists(_.name.name == table.baseTableRow.tableName))
      }
  }
}
