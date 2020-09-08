package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.option.optionInstance
import scalaz.syntax.applicative.ToApplyOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps._

// TODO replace with composition
sealed abstract class SlickReconStorage[R <: Reconcilable](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplateFromConf[R, StoredReconResult](ec, dbP) with ReconStorage[R] {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected implicit val reconIdColumn: JdbcType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID.apply)
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType
  override def isIgnored(k: R): Future[IgnoredReconResult] = load(k).map(_.isIgnored)
      .run.map(IgnoredReconResult.from)

  override protected def extractId(r: R) = r.normalize
}

@Singleton
class ArtistReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider
) extends SlickReconStorage[Artist](ec, dbP) {
  import profile.api._

  override protected type Entity = (String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist") {
    def name = column[String]("name", O.PrimaryKey)
    def reconId = column[Option[ReconID]]("recon_id")
    def isIgnored = column[Boolean]("is_ignored", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(a: Artist, v: StoredReconResult) = v match {
    case NoRecon => (a.normalize, None, true)
    case StoredReconResult.HasReconResult(reconId, isIgnored) => (a.normalize, Some(reconId), isIgnored)
  }
  override protected def toId(et: EntityTable) = et.name
  override protected def extractValue(e: Entity) =
    e._2.mapHeadOrElse(HasReconResult(_, e._3), NoRecon)
}

@Singleton
class AlbumReconStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider
) extends SlickReconStorage[Album](ec, dbP) {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override protected type Entity = (String, String, Option[ReconID], Boolean)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "album") {
    def albumArtist = column[String]("album_artist", O.PrimaryKey)
    def artist = column[String]("artist")
    def reconId = column[Option[ReconID]]("recon_id")
    def isIgnored = column[Boolean]("is_ignored", O.Default(false))
    def artistIndex = index("album_recon_artist_index", artist)
    def * = (albumArtist, artist, reconId, isIgnored)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(a: Album, v: StoredReconResult) = {
    val albumKey = a.normalize
    val artistKey = a.artist.normalize
    v match {
      case NoRecon => (albumKey, artistKey, None, true)
      case HasReconResult(reconId, isIgnored) => (albumKey, artistKey, Some(reconId), isIgnored)
    }
  }
  override protected def toId(et: EntityTable) = et.albumArtist
  override protected def extractValue(e: Entity) = e._3.mapHeadOrElse(HasReconResult(_, e._4), NoRecon)

  /** Delete all recons for albums for that artist */
  def deleteAllRecons(a: Artist): Future[Traversable[(String, Option[ReconID], Boolean)]] = {
    val artistRows = tableQuery.filter(_.artist === a.normalize)
    val existingRows = db.run(artistRows
        .map(e => (e.albumArtist, e.reconId, e.isIgnored))
        .result
        .map(_.map(e => (e._1, e._2, e._3))))
    existingRows `<*ByName` db.run(artistRows.delete)
  }
}
