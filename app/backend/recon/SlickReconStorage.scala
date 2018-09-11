package backend.recon

import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

// TODO replace with composition
sealed abstract class SlickReconStorage[R <: Reconcilable](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplateFromConf[R, (Option[ReconID], Boolean)](ec, dbP) with ReconStorage[R] {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected implicit val localDateTimeColumn: JdbcType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID)
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType
  override def isIgnored(k: R): Future[IgnoredReconResult] = load(k)
      .map(_.map(_._2))
      .map(IgnoredReconResult.from)

  override protected def extractId(r: R) = r.normalize
}

@Singleton
class ArtistReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider
) extends SlickReconStorage[Artist](ec, dbP) {
  import profile.api._

  override protected type Entity = (String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ARTISTS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def reconId = column[Option[ReconID]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(a: Artist, v: (Option[ReconID], Boolean)) =
    (a.normalize, v._1, v._2)
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) = e._2 -> e._3
}

@Singleton
class AlbumReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider
) extends SlickReconStorage[Album](ec, dbP) {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override protected type Entity = (String, String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "ALBUMS") {
    def album = column[String]("ALBUM", O.PrimaryKey)
    def artist = column[String]("ARTIST")
    def reconId = column[Option[ReconID]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def artistIndex = index("ARTIST_INDEX", artist)
    def * = (album, artist, reconId, isIgnored)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(a: Album, v: (Option[ReconID], Boolean)) =
    (a.normalize, a.artist.normalize, v._1, v._2)
  override protected def toId(et: EntityTable) = et.album
  override protected def extractValue(e: Entity) = e._3 -> e._4

  /** Delete all recons for albums for that artist */
  def deleteAllRecons(a: Artist): Future[Traversable[(String, Option[ReconID], Boolean)]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    val existingRows = db.run(artistRows
        .map(e => (e.album, e.reconId, e.isIgnored))
        .result
        .map(_.map(e => (e._1, e._2, e._3))))
    existingRows `<*ByName` db.run(artistRows.delete)
  }
}
