package backend.new_albums.filler.storage

import java.time.LocalDate

import backend.mb.AlbumType
import backend.new_albums.{AddedAlbumCount, ArtistNewAlbums, NewAlbum}
import backend.new_albums.filler.NewAlbumRecon
import backend.recon.{Artist, ReconID, SlickArtistReconStorage}
import backend.score.OptionalModelScore
import backend.score.storage.ArtistScoreStorage
import backend.storage.{DbProvider, JdbcMappers, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.Inject
import models.AlbumTitle
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.{catsSyntaxApplicativeByName, catsSyntaxApplyOps, catsSyntaxFlatMapOps, toFunctorOps, toTraverseFilterOps}
import common.rich.func.TuplePLenses.{__1, __2}
import common.rich.func.kats.Nesteds.SeqT
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.rich.RichT.richT
import common.rich.RichTime.OrderingLocalDate
import common.rich.RichTuple._
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichBoolean.richBoolean

// There's a bit of data/code duplication between this and SlickAlbumReconStorage, but the former is used only
// for already processed albums, and this one is for new albums.
private class SlickNewAlbumStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
    // Allows for easier cascade.
    protected val lastFetchTime: SlickLastFetchTimeStorage,
    protected val artistStorage: SlickArtistReconStorage,
    protected val artistScoreStorage: ArtistScoreStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[ReconID, StoredNewAlbum](ec, dbP)
    with NewAlbumStorage {
  private implicit val iec: ExecutionContext = ec
  import profile.api._

  override type Id = ReconID
  protected implicit override def btt: BaseTypedType[ReconID] =
    MappedColumnType.base[ReconID, String](_.id, ReconID.apply)
  private val mappers = new JdbcMappers()
  import mappers.ArtistMapper

  protected override def toEntity(k: ReconID, v: StoredNewAlbum): Entity = {
    val na = v.na
    (
      k,
      na.title.toLowerCase,
      na.albumType.toString,
      na.date,
      na.artist,
      v.isRemoved,
      v.isIgnored,
    )
  }
  protected override def extractId(k: ReconID) = k
  protected override def extractValue(e: Entity) = StoredNewAlbum(
    NewAlbum(
      title = e._2,
      date = e._4,
      artist = e._5,
      albumType = AlbumType.withName(e._3),
      reconID = e._1,
    ),
    isRemoved = e._6,
    isIgnored = e._7,
  )
  protected override def toId(et: Rows) = et.reconId
  private type AlbumType = String
  private type IsRemoved = Boolean
  private type IsIgnored = Boolean
  protected override type Entity =
    (ReconID, AlbumTitle, AlbumType, LocalDate, Artist, IsRemoved, IsIgnored)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "new_album") {
    def reconId = column[ReconID]("recon_id")
    def album = column[AlbumTitle]("album")
    // TODO why is this not an enum?
    def albumType = column[AlbumType]("type")
    def year = column[LocalDate]("epoch_day")
    def artist = column[Artist]("artist")
    def pk = primaryKey("album_artist_type", (album, artist, albumType))
    def artist_fk = foreignKey("artist_fk", artist, lastFetchTime.tableQuery)(
      _.artist,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def isRemoved = column[IsRemoved]("is_removed", O.Default(false))
    def isIgnored = column[IsIgnored]("is_ignored", O.Default(false))
    def artist_index = index("artist_index", artist, unique = false)
    def * =
      (reconId, album, albumType, year, artist, isRemoved, isIgnored)
  }
  protected override type EntityTable = Rows
  protected override val tableQuery = TableQuery[EntityTable]
  override def all: SeqT[Future, ArtistNewAlbums] = SeqT(
    db
      .run(
        tableQuery
          .join(artistStorage.tableQuery)
          .on(_.artist === _.artist)
          .filterNot(e => e._1.isIgnored || shouldRemoveAlbum(e._1))
          .map(_._1)
          .joinLeft(artistScoreStorage.tableQuery)
          .on(_.artist === _.artist)
          .result,
      )
      .map(
        _.view
          .map(__1.modify(extractValue))
          .map(__2.modify(_.map(_._2).toOptionalModelScore))
          .groupBy(_._1.na.artist)
          .view
          .mapValues(_.map(_.swap) |> toNewAlbums)
          .map(_.flatten)
          .map(Function.tupled(ArtistNewAlbums.apply))
          .toVectorSeq,
      ),
  )
  private def shouldRemoveAlbum(e: Rows): Rep[Boolean] = e.isRemoved || e.isIgnored
  private def toNewAlbums(
      zipped: Iterable[(OptionalModelScore, StoredNewAlbum)],
  ): (OptionalModelScore, Seq[NewAlbum]) = {
    val (scores, albums) = zipped.unzip
    (scores.toSet.single, toNewAlbums(albums))
  }
  private def toNewAlbums(albums: Iterable[StoredNewAlbum])(implicit
      dummy: DummyImplicit,
  ): Seq[NewAlbum] =
    albums.toVector.sortBy(_.na.date)(implicitly[Ordering[LocalDate]].reverse).reverse.map(_.na)
  override def apply(a: Artist) = db
    .run(
      tableQuery
        .filter(_.artist === a)
        .filterNot(shouldRemoveAlbum)
        .result,
    )
    .map(_.map(extractValue) |> toNewAlbums)
  override def unremoveAll(a: Artist) = db
    .run(
      tableQuery
        .filter(e => e.artist === a && e.isRemoved && !e.isIgnored)
        .map(_.isRemoved)
        .update(false),
    )
    .void
  private def toPartialEntity(
      a: NewAlbumRecon,
  ): (ReconID, AlbumTitle, AlbumType, LocalDate, Artist) = {
    val na = a.newAlbum
    (
      a.reconId,
      na.title.toLowerCase.joinOption(a.disambiguation)((title, disambi) => s"$title ($disambi)"),
      na.albumType.toString,
      na.date,
      na.artist,
    )
  }
  private def existsWithADifferentReconID(a: NewAlbumRecon): Future[Boolean] = db
    // TODO there is some duplication here with the above in the definition of the primary key.
    .run(
      tableQuery
        .filter { e =>
          val album = a.newAlbum
          e.album === album.title.toLowerCase &&
          e.artist === album.artist &&
          e.albumType === album.albumType.entryName &&
          e.reconId =!= a.reconId
        }
        .exists
        .result,
    )
    .listen(e => if (e) scribe.warn(s"<$a> already exists with a different ReconID... skipping"))
  private def isValid(e: NewAlbumRecon): Future[Boolean] =
    exists(e.reconId).map2(existsWithADifferentReconID(e))(_ neither _)
  override def storeNew(albums: Seq[NewAlbumRecon]): Future[AddedAlbumCount] = {
    val withoutDups = albums
      .groupBy(_.toTuple(_.newAlbum.artist, _.newAlbum.title, _.disambiguation))
      .values
      .map { e =>
        val v = e.toVector
        if (v.size > 1) {
          val albums = v.filter(_.newAlbum.albumType == AlbumType.Album)
          if (albums.size == 1)
            e.head
          else
            // This isn't the most elegant solution, as it crashes the entire request due to
            // failures from the API side, but it ensures those releases are merged.
            throw new Exception(
              s"Could not extract a single album out of <$v>. " +
                "This is probably caused by multiple release groups for the same album on " +
                "MusicBrainz's side, which should be merged.",
            )
        } else
          v.head
      }
      .toVector
    for {
      newAlbums <- withoutDups.filterA(isValid)
      result = newAlbums.size
      _ <- db
        .run(
          tableQuery
            .map(_.toTuple(_.reconId, _.album, _.albumType, _.year, _.artist))
            .++=(newAlbums.map(toPartialEntity).ensuring(_.nonEmpty)),
        )
        .whenA(result > 0)
        .listenError(scribe.error(s"Failed to store albums: <${newAlbums.mkString("\n")}>", _))
    } yield result
  }
  override def remove(artist: Artist) =
    db.run(tableQuery.filter(e => e.artist === artist).map(_.isRemoved).update(true)).void

  private def updateAlbum(
      f: EntityTable => Rep[Boolean],
  )(reconID: ReconID): Future[Unit] = {
    val filter = tableQuery.filter(_.reconId === reconID)
    for {
      albumExists <- db.run(filter.exists.result)
      _ <- Future
        .failed(new IllegalArgumentException(s"Could not find album with ID <${reconID.id}>"))
        .whenA(albumExists.isFalse)
      _ <- db.run(filter.map(f).update(true))
    } yield ()
  }
  override def removeAlbum(reconID: ReconID) = updateAlbum(_.isRemoved)(reconID)
  override def ignoreAlbum(reconID: ReconID) =
    removeAlbum(reconID) >> updateAlbum(_.isIgnored)(reconID)
  override def deleteAll(artist: Artist) =
    db.run(tableQuery.filter(e => e.artist === artist).delete).void
}
