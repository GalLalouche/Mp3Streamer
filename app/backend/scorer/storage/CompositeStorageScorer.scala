package backend.scorer.storage

import javax.inject.Inject

import backend.recon.{Album, Artist, Track}
import backend.recon.Reconcilable._
import backend.scorer.{CachedModelScorerState, CompositeScorer, FullInfoModelScorer, FullInfoScore, OptionalModelScore}
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToBindOpsUnapply

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private[scorer] class CompositeStorageScorer @Inject() (
    trackScorer: StorageScorer[Track],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    cachedModelScorerState: CachedModelScorerState,
    ec: ExecutionContext,
) extends FullInfoModelScorer {
  private implicit val iec: ExecutionContext = ec
  override def apply(s: Song): Future[FullInfoScore] = aux(s.track)

  private val aux =
    new CompositeScorer[Future](trackScorer.apply, albumScorer.apply, artistScorer.apply)

  override def updateSongScore(song: Song, score: OptionalModelScore) =
    trackScorer.updateScore(song.track, score) >> cachedModelScorerState.update()
  override def updateAlbumScore(song: Song, score: OptionalModelScore) =
    albumScorer.updateScore(song.release, score) >> cachedModelScorerState.update()
  override def updateArtistScore(song: Song, score: OptionalModelScore) =
    artistScorer.updateScore(song.artist, score) >> cachedModelScorerState.update()
}
