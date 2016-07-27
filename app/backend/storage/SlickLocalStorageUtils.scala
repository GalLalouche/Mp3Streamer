package backend.storage

import backend.Configuration
import common.RichFuture._
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}

object SlickLocalStorageUtils {
  def toBoolean(f: Future[_])(implicit ec: ExecutionContext): Future[Boolean] = f.map(e => true) orElse false
  def apply[T](c: Configuration) = {
    import c._
    import c.driver.api._
    val db = c.db
    new {
      def apply(table: TableQuery[_ <: Table[_]]): LocalStorageUtils = new LocalStorageUtils {
        override def createTable(): Future[Boolean] =
          toBoolean((db run table.schema.create))
        override def clearTable(): Future[Boolean] =
          toBoolean(db run table.delete)
        override def dropTable(): Future[Boolean] =
          toBoolean(db run table.schema.drop)
        override def doesTableExist(): Future[Boolean] =
          db run MTable.getTables map (tables => tables.exists(_.name.name == table.baseTableRow.tableName))
      }
    }
  }
}
