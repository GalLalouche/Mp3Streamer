package backend.albums.filler.storage

import java.time.LocalDateTime
import backend.recon.{Artist, SlickArtistReconStorage}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import javax.inject.Inject
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.ExecutionContext

// TODO Unit RefreshableStorage
private class SlickLastFetchTimeStorage @Inject()(
    ec: ExecutionContext, dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Artist, Option[LocalDateTime]](ec, dbP) {

  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override type Id = String
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType
  // TODO code duplication with SlickExternalStorage
  override protected def extractId(k: Artist) = k.normalize
  override type Entity = (String, Option[LocalDateTime])
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist_last_album_update") {
    def name = column[String]("name", O.PrimaryKey)
    def artist_fk = foreignKey("artist_fk", name, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def timestamp = column[Option[LocalDateTime]]("timestamp")
    def * = (name, timestamp)
  }
  override protected type EntityTable = Rows
  override val tableQuery = TableQuery[EntityTable]
  override protected def toId(et: Rows) = et.name
  override protected def toEntity(k: Artist, v: Option[LocalDateTime]) = (k.normalize, v)
  override protected def extractValue(e: (String, Option[LocalDateTime])) = e._2
}
