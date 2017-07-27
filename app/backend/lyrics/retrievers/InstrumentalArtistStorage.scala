package backend.lyrics.retrievers

import backend.configs.Configuration
import backend.storage.{SlickStorageUtils, StorageTemplate}

import scala.concurrent.Future

class InstrumentalArtistStorage(implicit c: Configuration) extends StorageTemplate[String, Unit] {
  import c.driver.api._
  private val db = c.db

  // instrumental songs have NULL in lyrics
  private class ArtistTable(tag: Tag) extends Table[(String)](tag, "INSTRUMENTAL_ARTISTS") {
    def artistName = column[String]("ARTIST_NAME", O.PrimaryKey)
    def * = artistName
  }
  private val rows = TableQuery[ArtistTable]
  private def normalize(artistName: String): String = artistName.toLowerCase

  override protected def internalForceStore(artistName: String, v: Unit) =
    db run rows.insertOrUpdate(normalize(artistName))
  def store(artistName: String): Future[Boolean] = store(artistName: String, ())
  override protected def internalDelete(artistName: String) =
    db.run(rows.filter(_.artistName === normalize(artistName)).delete)
  override def load(artistName: String) =
    db.run(rows.filter(_.artistName === normalize(artistName)).result)
        .map(_.ensuring(_.lengthCompare(2) < 0).nonEmpty)
        .map(if (_) Some(Unit) else None)
  override def utils = SlickStorageUtils(c)(rows)
}
