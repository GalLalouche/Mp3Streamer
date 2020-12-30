package backend.albums.filler.storage

import java.time.LocalDate

import backend.albums.filler.NewAlbumRecon
import backend.albums.NewAlbum
import backend.logging.Logger
import backend.mb.AlbumType
import backend.module.StandaloneModule
import backend.recon.{Artist, ReconID}
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.Guice
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.std.vector.vectorInstance
import scalaz.syntax.apply.^
import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreApplicativeOps.toLazyApplicativeUnitOps
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.func.ToTraverseMonadPlusOps._

import common.rich.RichFuture.richFuture
import common.rich.RichT._
import common.rich.RichTime.OrderingLocalDate

// There's a bit of data/code duplication between this and SlickAlbumReconStorage, but the former is used only
// for already processed albums, and this one is for new albums.
private class SlickNewAlbumStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    // Allows for easier cascade.
    protected val artistStorage: SlickLastFetchTimeStorage,
    logger: Logger,
) extends SlickSingleKeyColumnStorageTemplateFromConf[ReconID, StoredNewAlbum](ec, dbP) with NewAlbumStorage {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override type Id = ReconID
  override protected implicit def btt: BaseTypedType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID.apply)
  override protected def toEntity(k: ReconID, v: StoredNewAlbum): Entity = {
    val na = v.na
    (k, na.title.toLowerCase, na.albumType.toString, na.date, na.artist.normalize, v.isRemoved, v.isIgnored)
  }
  override protected def extractId(k: ReconID) = k
  override protected def extractValue(e: Entity) = StoredNewAlbum(
    NewAlbum(
      title = e._2,
      date = e._4,
      artist = Artist(e._5),
      albumType = AlbumType.withName(e._3),
    ),
    isRemoved = e._6,
    isIgnored = e._7,
  )
  override protected def toId(et: Rows) = et.reconId
  private type AlbumTitle = String
  private type AlbumType = String
  private type ArtistName = String
  private type IsRemoved = Boolean
  private type IsIgnored = Boolean
  override protected type Entity =
    (ReconID, AlbumTitle, AlbumType, LocalDate, ArtistName, IsRemoved, IsIgnored)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "new_album") {
    def reconId = column[ReconID]("recon_id")
    def album = column[AlbumTitle]("album")
    // TODO why is this not an enum?
    def albumType = column[AlbumType]("type")
    def year = column[LocalDate]("epoch_day")
    def artist = column[ArtistName]("artist")
    def pk = primaryKey("album_artist_type", (album, artist, albumType))
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
  override def all = ListT(db
      .run(tableQuery.filter(e => !(e.isRemoved || e.isIgnored)).result)
      .map(_.map(extractValue)
          .groupBy(_.na.artist)
          .mapValues(_.sortBy(_.na.date).map(_.na))
          .toList
      )
  )
  override def unremoveAll(a: Artist) = db.run(tableQuery
      .filter(e => e.artist === a.normalize && e.isRemoved && !e.isIgnored)
      .map(_.isRemoved)
      .update(false)
  ).void
  private def toPartialEntity(a: NewAlbumRecon): (ReconID, AlbumTitle, AlbumType, LocalDate, ArtistName) = {
    val na = a.newAlbum
    (a.reconId, na.title.toLowerCase, na.albumType.toString, na.date, na.artist.normalize)
  }
  private def existsWithADifferentReconID(a: NewAlbumRecon): Future[Boolean] = db
      // TODO there is some duplication here with the above in the definition of the primary key.
      .run(
        tableQuery.filter(e => {
          val album = a.newAlbum
          e.album === album.title.toLowerCase &&
              e.artist === album.artist.normalize &&
              e.albumType === album.albumType.entryName &&
              e.reconId =!= a.reconId
        }).exists.result)
      .listen(e =>
        if (e) logger.warn(s"<$a> already exists with a different ReconID... skipping")
      )
  private def isInvalid(e: NewAlbumRecon): Future[Boolean] =
  // TODO RichBoolean.neither?
    ^(exists(e.reconId), existsWithADifferentReconID(e))(_ || _)
  override def storeNew(albums: Seq[NewAlbumRecon]): Future[Int] = {
    val withoutDups = albums
        .groupBy(_.toTuple(_.newAlbum.artist, _.newAlbum.title))
        .mapValues {
          e =>
            val v = e.toVector
            if (v.size > 1) {
              val albums = v.filter(_.newAlbum.albumType == AlbumType.Album)
              if (albums.size == 1)
                e.head
              else
                throw new AssertionError(s"Could not extract a single album out of <$v>")
            } else
              v.head
        }
        .values
        .toVector
    for {
      newAlbums <- withoutDups.filterM(isInvalid(_).negated)
      result = newAlbums.size
      _ <- db.run(tableQuery
          .map(_.toTuple(_.reconId, _.album, _.albumType, _.year, _.artist))
          .++=(newAlbums.map(toPartialEntity).ensuring(_.nonEmpty))
      )
          .whenMLazy(result > 0)
          .listenError(logger.error(s"Failed to store albums: <${newAlbums mkString "\n"}>", _))
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