package backend.scorer

import backend.recon.Artist
import backend.scorer.FullArtistScores.ArtistScore
import models.{IOSong, Song}
import scala.concurrent.Future

private trait FullInfoModelScorer {
  def apply(s: Song): Future[FullInfoScore]
  def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit]
  def openScoreFile(song: Song): Future[Unit]
}
