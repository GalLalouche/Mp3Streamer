package backend.albums.filler.storage

import java.time.LocalDateTime
import javax.inject.Inject

import backend.recon.{Artist, SlickArtistReconStorage}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.ExecutionContext

// TODO Unit RefreshableStorage
private class SlickLastFetchTimeStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Artist, Option[LocalDateTime]](ec, dbP) {
  import profile.api._

  override type Id = String
  protected implicit override def btt: BaseTypedType[String] = ScalaBaseType.stringType
  // TODO code duplication with SlickExternalStorage
  protected override def extractId(k: Artist) = k.normalize
  override type Entity = (String, Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist_last_album_update") {
    def name = column[String]("name", O.PrimaryKey)
    def artist = name.mapTo[Artist]
    def artist_fk = foreignKey("artist_fk", name, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def timestamp = column[Option[LocalDateTime]]("timestamp")
    def * = (name, timestamp)
  }
  protected override type EntityTable = Rows
  override val tableQuery = TableQuery[EntityTable]
  protected override def toId(et: Rows) = et.name
  protected override def toEntity(k: Artist, v: Option[LocalDateTime]) = (k.normalize, v)
  protected override def extractValue(e: (String, Option[LocalDateTime])) = e._2
}
