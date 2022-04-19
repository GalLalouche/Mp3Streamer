package backend.scorer.storage

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable._
import backend.scorer.{CompositeScorer, FullInfoModelScorer, ModelScore}
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

import common.rich.func.BetterFutureInstances._

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private[scorer] class CompositeStorageScorer @Inject()(
    songScorer: StorageScorer[Song],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    ec: ExecutionContext,
) extends FullInfoModelScorer {
  private implicit val iec: ExecutionContext = ec
  private val aux = new CompositeScorer[Future](
    songScorer.apply,
    albumScorer.apply,
    artistScorer.apply,
  )
  override def apply(s: Song): Future[FullInfoModelScorer.SongScore] = aux(s)
  override def updateSongScore(song: Song, score: ModelScore) = songScorer.updateScore(song, score)
  override def updateAlbumScore(song: Song, score: ModelScore) = albumScorer.updateScore(song.release, score)
  override def updateArtistScore(song: Song, score: ModelScore) = artistScorer.updateScore(song.artist, score)
}
