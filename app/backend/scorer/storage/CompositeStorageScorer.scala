package backend.scorer.storage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scalaz.Scalaz.ToBindOpsUnapply

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable._
import backend.scorer.{CachedModelScorerState, CompositeScorer, FullInfoModelScorer, FullInfoScore, ModelScore}
import common.rich.func.BetterFutureInstances._
import models.Song

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private[scorer] class CompositeStorageScorer @Inject() (
    songScorer: StorageScorer[Song],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    cachedModelScorerState: CachedModelScorerState,
    ec: ExecutionContext,
) extends FullInfoModelScorer {
  private implicit val iec: ExecutionContext = ec
  private val aux = new CompositeScorer[Future](
    songScorer.apply,
    albumScorer.apply,
    artistScorer.apply,
  )
  override def apply(s: Song): Future[FullInfoScore] = aux(s)
  override def updateSongScore(song: Song, score: ModelScore) =
    songScorer.updateScore(song, score) >> cachedModelScorerState.update()
  override def updateAlbumScore(song: Song, score: ModelScore) =
    albumScorer.updateScore(song.release, score) >> cachedModelScorerState.update()
  override def updateArtistScore(song: Song, score: ModelScore) =
    artistScorer.updateScore(song.artist, score) >> cachedModelScorerState.update()
}
