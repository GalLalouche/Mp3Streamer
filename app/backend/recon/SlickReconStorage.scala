package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf, SlickStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.{JdbcProfile, JdbcType}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import scalaz.OptionT
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps._

private class SlickReconStorageAux(profile: JdbcProfile)(implicit ec: ExecutionContext) {
  import profile.api._

  implicit val reconIdColumn: JdbcType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID.apply)
  def btt: BaseTypedType[String] = ScalaBaseType.stringType
  def isIgnored(srr: OptionT[Future, StoredReconResult]): Future[IgnoredReconResult] =
    srr.map(_.isIgnored).run.map(IgnoredReconResult.from)
}
@Singleton
private[backend] class SlickArtistReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider
) extends SlickSingleKeyColumnStorageTemplateFromConf[Artist, StoredReconResult](ec, dbP)
    with ArtistReconStorage with ReconStorage[Artist] {
  private implicit val iec: ExecutionContext = ec
  private val aux = new SlickReconStorageAux(profile)
  import aux.reconIdColumn

  override protected implicit def btt: BaseTypedType[String] = aux.btt
  override def isIgnored(k: Artist): Future[IgnoredReconResult] = aux.isIgnored(load(k))

  override protected type Id = String

  override protected def extractId(a: Artist) = a.normalize
  import profile.api._

  override type Entity = (String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist") {
    def name = column[String]("name", O.PrimaryKey)
    def reconId = column[Option[ReconID]]("recon_id")
    // TODO remove isIgnored
    def isIgnored = column[Boolean]("is_ignored", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  override protected type EntityTable = Rows
  val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(a: Artist, v: StoredReconResult) = v match {
    case NoRecon => (a.normalize, None, true)
    case StoredReconResult.HasReconResult(reconId, isIgnored) => (a.normalize, Some(reconId), isIgnored)
  }
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) =
    e._2.mapHeadOrElse(HasReconResult(_, e._3), NoRecon)
}

@Singleton
private[backend] class SlickAlbumReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Album, StoredReconResult](ec, dbP) with AlbumReconStorage {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  private val aux = new SlickReconStorageAux(profile)
  import aux.reconIdColumn

  override def isIgnored(k: Album): Future[IgnoredReconResult] = aux.isIgnored(load(k))
  protected override type Entity = (String, String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "album") {
    def album = column[String]("album") // Normalized title, basically.
    def artist = column[String]("artist")
    def pk = primaryKey("album_artist", (album, artist))
    def artist_fk = foreignKey("artist_fk", artist, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def reconId = column[Option[ReconID]]("recon_id")
    // TODO remove isIgnored
    def isIgnored = column[Boolean]("is_ignored", O.Default(false))
    def * = (album, artist, reconId, isIgnored)
  }
  protected override type EntityTable = Rows
  override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(a: Album, v: StoredReconResult) = {
    val albumKey = a.normalize
    val artistKey = a.artist.normalize
    v match {
      case NoRecon => (albumKey, artistKey, None, true)
      case HasReconResult(reconId, isIgnored) => (albumKey, artistKey, Some(reconId), isIgnored)
    }
  }

  override protected def keyFilter(a: Album)(e: EntityTable) =
    e.artist === a.artist.normalize && e.album === a.normalize
  override protected def extractValue(e: Entity) =
    e._3.mapHeadOrElse(HasReconResult(_, e._4), NoRecon)
}
