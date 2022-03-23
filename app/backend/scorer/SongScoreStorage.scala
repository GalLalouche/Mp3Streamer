package backend.scorer

import backend.recon.{Artist, SlickAlbumReconStorage, SlickArtistReconStorage}

import common.rich.func.BetterFutureInstances._
import backend.recon.Reconcilable.SongExtractor
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.Scalaz.ToFunctorOps

private class SongScoreStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Song, ModelScore](ec, dbP)
    with StorageScorer[Song] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  type AlbumTitle = String
  type SongTitle = String
  override protected type Entity = (Artist, AlbumTitle, SongTitle, ModelScore)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "song_score") {
    def artist = column[Artist]("artist")
    def album = column[AlbumTitle]("album")
    def song = column[SongTitle]("song")
    def score = column[ModelScore]("score")
    def pk = primaryKey(dbP.constraintMangler("pk"), (artist, album, song))
    def artist_fk =
      foreignKey(
        dbP.constraintMangler("artist_fk"), artist, artistStorage.tableQuery)(
        _.name.mapTo[Artist])
    def * = (artist, album, song, score)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Song, v: ModelScore) = (k.artist, k.albumName, k.title, v)
  override protected def extractValue(e: (Artist, AlbumTitle, SongTitle, ModelScore)) = e._4
  override protected def keyFilter(k: Song)(e: Rows) =
    e.artist === k.artist && e.song === k.title && e.album === k.albumName
  override def apply(a: Song) = load(a)
  def loadAll: ListT[Future, (Artist, AlbumTitle, SongTitle, ModelScore)] =
    ListT(db.run(tableQuery.result).map(_.toList))
  override def updateScore(a: Song, score: ModelScore) = replace(a, score).run.void
}
