package backend.score.scorer

import backend.score.model.{FullInfoScore, OptionalModelScore}
import models.Song

import scala.concurrent.Future

private[score] trait ScoreModel {
  def apply(s: Song): FullInfoScore
  def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit]
  def openScoreFile(song: Song): Future[Unit]
}
