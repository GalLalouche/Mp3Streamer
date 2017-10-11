package backend.lyrics.retrievers

import backend.configs.Configuration
import backend.storage.SlickStorageTemplate
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.Future

class InstrumentalArtistStorage(implicit _c: Configuration) extends SlickStorageTemplate[String, Unit] {
  import c.profile.api._
  private def normalize(artistName: String): String = artistName.toLowerCase

  override protected type Id = String
  override protected type Entity = String
  protected class ArtistTable(tag: Tag) extends Table[(String)](tag, "INSTRUMENTAL_ARTISTS") {
    def artistName = column[String]("ARTIST_NAME", O.PrimaryKey)
    def * = artistName
  }
  override protected type EntityTable = ArtistTable
  override protected val tableQuery = TableQuery[ArtistTable]
  override protected def toEntity(k: String, v: Unit) = normalize(k)
  override protected def extractId(k: String) = normalize(k)
  override protected def toId(et: ArtistTable) = et.artistName
  override protected def extractValue(e: String): Unit = ()
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType

  def store(artistName: String): Future[Boolean] = store(artistName: String, ())
}
