package backend.lyrics.retrievers

import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.Future

class InstrumentalArtistStorage(implicit _dbP: DbProvider) extends SlickStorageTemplateFromConf[String, Unit] {
  import profile.api._

  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected type Entity = String
  protected class ArtistTable(tag: Tag) extends Table[Entity](tag, "INSTRUMENTAL_ARTISTS") {
    def artistName = column[String]("ARTIST_NAME", O.PrimaryKey)
    def * = artistName
  }
  override protected type EntityTable = ArtistTable
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: String, v: Unit) = extractId(k)
  override protected def extractId(k: String) = k.toLowerCase
  override protected def toId(et: ArtistTable) = et.artistName
  override protected def extractValue(e: String): Unit = ()

  def store(artistName: String): Future[Unit] = store(artistName: String, ())
}
