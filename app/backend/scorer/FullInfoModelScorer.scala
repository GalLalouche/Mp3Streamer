package backend.scorer

import scala.concurrent.Future

import models.Song

private trait FullInfoModelScorer {
  def apply(s: Song): Future[FullInfoScore]
  def updateSongScore(song: Song, score: ModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: ModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: ModelScore): Future[Unit]
}
