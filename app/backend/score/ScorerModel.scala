package backend.score

import backend.recon.{Album, Artist, Track}
import backend.recon.Reconcilable._
import backend.score.file.FileScorer
import backend.score.storage.StorageScorer
import com.google.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.catsSyntaxFlatMapOps

private class ScorerModel @Inject() (
    trackScorer: StorageScorer[Track],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    cachedModelScorerState: CachedModelScorerState,
    ec: ExecutionContext,
    fileScorer: FileScorer,
) extends FullInfoModelScorer {
  override def apply(s: Song): Future[FullInfoScore] =
    Future.successful(cachedModelScorerState.fullInfo(s.track))

  private implicit val iec: ExecutionContext = ec

  override def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit] =
    trackScorer.updateScore(song.track, score) >> cachedModelScorerState.update()
  override def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit] =
    albumScorer.updateScore(song.release, score) >> cachedModelScorerState.update()
  override def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit] =
    artistScorer.updateScore(song.artist, score) >> cachedModelScorerState.update()

  def openScoreFile(song: Song): Future[Unit] = fileScorer(song) >> cachedModelScorerState.update()
}
