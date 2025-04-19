package backend.scorer

import models.Song

import scala.concurrent.Future

private trait FullInfoModelScorer {
  def apply(s: Song): Future[FullInfoScore]
  def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit]
  /**
   * Mass editing all of an artist's albums and tracks using an external text editor. Returns a
   * [[Future]] which will complete when the file has been closed and the updated scores processed.
   */
  def openScoreFile(song: Song): Future[Unit]
}
