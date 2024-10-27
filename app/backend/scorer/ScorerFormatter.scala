package backend.scorer

import java.io.File
import javax.inject.Inject
import backend.recon.Artist
import backend.scorer.FullArtistScores.{AlbumScore, ArtistScore, SongScore}
import mains.fixer.StringFixer
import models.{IOSong, Song, SongTagParser}
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.{ExecutionContext, Future}
import monocle.Monocle.toApplyLensOps
import common.json.JsonWriteable
import common.json.ToJsonableOps.{jsonifySingle, _}

/** Fetches and updates scores for songs, albums, and artists. */
private class ScorerFormatter @Inject() (
    modelScorer: FullInfoModelScorer,
    ec: ExecutionContext,
) {
  import ScorerFormatter._
  private implicit val iec: ExecutionContext = ec
  def getScore(filePath: String): Future[JsValue] =
    modelScorer(toSong(filePath)).map(_.jsonify)
  def openScoreFile(filePath: String): Future[Unit] =
    modelScorer.openScoreFile(toSong(filePath))
  private def update(
      f: (Song, OptionalModelScore) => Future[Unit],
      filePath: String,
      score: String,
  ) = f(toSong(filePath), OptionalModelScore.withNameInsensitive(score))
  def updateSongScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateSongScore, filePath, score)
  def updateAlbumScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateAlbumScore, filePath, score)
  def updateArtistScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateArtistScore, filePath, score)
}

private object ScorerFormatter {
  private def toSong(path: String): Song = SongTagParser(new File(path))

  private implicit val songScoreJsonable: JsonWriteable[FullInfoScore] = {
    case FullInfoScore.Default => Json.obj()
    case FullInfoScore.Scored(score, source, song, album, artist) =>
      Json.obj(
        "score" -> score.entryName,
        "source" -> source.toString,
        "song" -> song.entryName,
        "album" -> album.entryName,
        "artist" -> artist.entryName,
      )
  }
}
