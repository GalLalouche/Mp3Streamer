package backend.scorer.storage

import com.google.inject.Inject

import backend.recon.{Artist, SlickArtistReconStorage, Track, YearlessAlbum, YearlessTrack}
import backend.scorer.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickStorageTemplateFromConf}
import models.{AlbumTitle, SongTitle}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.ListT

private[scorer] class TrackScoreStorage @Inject() (
    protected override val ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Track, ModelScore](ec, dbP)
    with StorageScorer[Track] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  // TODO can this use YearlessTrack?
  protected override type Entity = (Artist, AlbumTitle, SongTitle, ModelScore)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "song_score") {
    def artist = column[Artist]("artist")
    def album = column[AlbumTitle]("album")
    def song = column[SongTitle]("song")
    def score = column[ModelScore]("score")
    def pk = primaryKey(dbP.constraintMangler("pk"), (artist, album, song))
    def artist_fk =
      foreignKey(dbP.constraintMangler("artist_fk"), artist, artistStorage.tableQuery)(
        _.name.mapTo[Artist],
      )
    def * = (artist, album, song, score)
  }
  protected override type EntityTable = Rows
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(k: Track, v: ModelScore) =
    (k.artist, k.album.title.toLowerCase, k.title.toLowerCase, v)
  protected override def extractValue(e: (Artist, AlbumTitle, SongTitle, ModelScore)) = e._4
  protected override def keyFilter(k: Track)(e: Rows) =
    e.artist === k.artist &&
      e.song === k.title.toLowerCase &&
      e.album === k.album.title.toLowerCase
  def loadAll: ListT[Future, (YearlessTrack, ModelScore)] =
    ListT(db.run(tableQuery.result).map(_.toList))
      .map(e => (YearlessTrack(e._3, YearlessAlbum(e._2, e._1)), e._4))
}
