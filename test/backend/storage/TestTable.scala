package backend.storage

import backend.configs.Configuration
import slick.ast.{BaseTypedType, ScalaBaseType}

private class TestTable(implicit _c: Configuration) extends SlickStorageTemplate[Int, String] {
  import c.profile.api._

  override protected type Id = Int
  override protected implicit def btt: BaseTypedType[Int] = ScalaBaseType.intType
  override protected type Entity = (Int, String)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "TABLE") {
    def key = column[Int]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Int, v: String) = k -> v
  override protected def extractId(k: Int) = k
  override protected def toId(et: EntityTable) = et.key
  override protected def extractValue(e: Entity) = e._2
}
