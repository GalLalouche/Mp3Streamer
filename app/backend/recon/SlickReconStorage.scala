package backend.recon

import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.jdbc.JdbcType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.OptionT
import scalaz.std.option.optionInstance
import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.applicative.ToApplyOps
import common.rich.func.ToMoreFoldableOps._

// TODO replace with composition
sealed abstract class SlickReconStorage[R <: Reconcilable](ec: ExecutionContext, dbP: DbProvider)
    extends SlickStorageTemplateFromConf[R, StoredReconResult](ec, dbP) with ReconStorage[R] {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  protected implicit val reconIdColumn: JdbcType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID)
  override protected type Id = String
  override protected implicit def btt: BaseTypedType[String] = ScalaBaseType.stringType
  override def isIgnored(k: R): Future[IgnoredReconResult] = OptionT(load(k))
      .map(_.isIgnored)
      .run
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
  override protected def toEntity(a: Album, v: StoredReconResult) = {
    val albumKey = a.normalize
    val artistKey = a.artist.normalize
    v match {
      case NoRecon => (albumKey, artistKey, None, true)
      case HasReconResult(reconId, isIgnored) => (albumKey, artistKey, Some(reconId), isIgnored)
    }
  }
  override protected def toId(et: EntityTable) = et.album
  override protected def extractValue(e: Entity) = e._3.mapHeadOrElse(HasReconResult(_, e._4), NoRecon)

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
