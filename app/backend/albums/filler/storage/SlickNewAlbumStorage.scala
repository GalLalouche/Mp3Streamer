package backend.albums.filler.storage

import backend.albums.NewAlbum
import backend.albums.filler.NewAlbumRecon
import backend.mb.AlbumType
import backend.module.StandaloneModule
import backend.recon.{Artist, ReconID}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.Guice
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Scalaz.{ToBindOps, ToFunctorOps}
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreSeqInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreApplicativeOps.toLazyApplicativeUnitOps
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToTraverseMonadPlusOps._

import common.rich.RichFuture.richFuture
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

// There's a bit of data/code duplication between this and SlickAlbumReconStorage, but the former is used only
// for already processed albums, and this one is for new albums.
private class SlickNewAlbumStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    // Allows for easier cascade.
    protected val artistStorage: SlickLastFetchTimeStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[ReconID, StoredNewAlbum](ec, dbP) with NewAlbumStorage {
  private implicit val iec: ExecutionContext = ec;
  import profile.api._

  override type Id = ReconID
  override protected implicit def btt: BaseTypedType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID.apply)
  override protected def toEntity(k: ReconID, v: StoredNewAlbum) = {
    val na = v.na
    (k, na.title.toLowerCase, na.albumType.toString, na.year, na.artist.normalize, v.isRemoved, v.isIgnored)
  }
  override protected def extractId(k: ReconID) = k
  override protected def extractValue(e: Entity) =
    StoredNewAlbum(
      NewAlbum(
        title = e._2,
        year = e._4,
        artist = Artist(e._5),
        albumType = AlbumType.withName(e._3),
      ),
      isRemoved = e._6,
      isIgnored = e._7,
    )
  override protected def toId(et: Rows) = et.reconId
  private type AlbumTitle = String
  private type AlbumType = String
  private type Year = Int
  private type ArtistName = String
  private type IsRemoved = Boolean
  private type IsIgnored = Boolean
  override protected type Entity = (ReconID, AlbumTitle, AlbumType, Year, ArtistName, IsRemoved, IsIgnored)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "new_album") {
    def reconId = column[ReconID]("recon_id")
    def album = column[AlbumTitle]("album")
    def albumType = column[AlbumType]("type")
    def year = column[Year]("year")
    def artist = column[ArtistName]("artist")
    def pk = primaryKey("album_artist", (album, artist))
    def artist_fk = foreignKey("artist_fk", artist, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def isRemoved = column[IsRemoved]("is_removed", O.Default(false))
    def isIgnored = column[IsIgnored]("is_ignored", O.Default(false))
    def artist_index = index("artist_index", artist, unique = false)
    def * =
      (reconId, album, albumType, year, artist, isRemoved, isIgnored)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override def all = db
      .run(tableQuery.filter(e => !(e.isRemoved || e.isIgnored)).result)
      .map(_.map(extractValue(_).na)
          .groupBy(_.artist)
          .mapValues(_.sortBy(_.year))
      )
  override def unremoveAll(a: Artist) = db.run(tableQuery
      .filter(e => e.artist === a.normalize && e.isRemoved && !e.isIgnored)
      .map(_.isRemoved)
      .update(false)
  ).void
  private def toPartialEntity(a: NewAlbumRecon): (ReconID, AlbumTitle, AlbumType, Year, ArtistName) = {
    val na = a.newAlbum
    (a.reconId, na.title.toLowerCase, na.albumType.toString, na.year, na.artist.normalize)
  }
  override def storeNew(albums: Seq[NewAlbumRecon]): Future[Int] = {
    if (!albums.map(_.toTuple(_.newAlbum.artist, _.newAlbum.title)).allUnique) {
      println(albums.toString)
      throw new AssertionError(albums.toString)
    }
    for {
      newAlbums <- albums.filterM(e => exists(e.reconId).negated)
      result = newAlbums.size
      _ <- db.run(tableQuery
          .map(_.toTuple(_.reconId, _.album, _.albumType, _.year, _.artist))
          .++=(newAlbums.map(toPartialEntity).ensuring(_.nonEmpty))
      ).whenMLazy(result > 0)
    } yield result
  }
  override def remove(artist: Artist) = db.run(tableQuery
      .filter(e => e.artist === artist.normalize)
      .map(_.isRemoved)
      .update(true)
  ).void

  private def updateAlbum(f: EntityTable => Rep[Boolean])(artist: Artist, albumName: String): Future[Unit] =
    db.run(tableQuery
        .filter(e => e.artist === artist.normalize && e.album === albumName.toLowerCase)
        .map(f)
        .update(true)
    ).void
  override def remove(artist: Artist, albumName: String) = updateAlbum(_.isRemoved)(artist, albumName)
  override def ignore(artist: Artist, albumName: String) =
    remove(artist, albumName) >> updateAlbum(_.isIgnored)(artist, albumName)
}

private object SlickNewAlbumStorage {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(StandaloneModule, FillerStorageModule)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector
        .instance[SlickNewAlbumStorage]
        .utils.createTableIfNotExists()
        .get
  }
}