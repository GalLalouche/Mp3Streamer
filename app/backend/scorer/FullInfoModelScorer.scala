package backend.scorer

import models.Song

import scala.concurrent.Future

private trait FullInfoModelScorer {
  def apply(s: Song): Future[FullInfoScore]
  def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit]
}
