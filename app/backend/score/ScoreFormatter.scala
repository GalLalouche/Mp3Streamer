package backend.score

import java.io.File

import backend.score.ScoreFormatter.toSong
import backend.score.model.{FullInfoScore, OptionalModelScore}
import backend.score.scorer.ScoreModel
import com.google.inject.Inject
import models.{IOSongTagParser, Song}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.Future

import common.json.JsonWriteable
import common.json.ToJsonableOps.jsonifySingle

/** Fetches and updates scores for songs, albums, and artists. */
class ScoreFormatter @Inject() ($ : ScoreModel) {
  import ScoreFormatter.songScoreJsonable
  def getScore(filePath: String): JsValue =
    $(IOSongTagParser(new File(filePath))).jsonify

  def updateSongScore(filePath: String, score: String): Future[Unit] =
    update($.updateSongScore, filePath, score)
  def updateAlbumScore(filePath: String, score: String): Future[Unit] =
    update($.updateAlbumScore, filePath, score)
  def updateArtistScore(filePath: String, score: String): Future[Unit] =
    update($.updateArtistScore, filePath, score)
  private def update(
      f: (Song, OptionalModelScore) => Future[Unit],
      filePath: String,
      score: String,
  ) = f(toSong(filePath), OptionalModelScore.withNameInsensitive(score))

  def openScoreFile(filePath: String): Future[Unit] = $.openScoreFile(toSong(filePath))
}

private object ScoreFormatter {
  private def toSong(path: String): Song = IOSongTagParser(new File(path))

  private implicit val songScoreJsonable: JsonWriteable[FullInfoScore] = {
    case FullInfoScore.Default => Json.obj()
    case scored: FullInfoScore.Scored =>
      Json.obj(
        "score" -> scored.score.entryName,
        "source" -> scored.source.toString,
        "song" -> scored.songScore.entryName,
        "album" -> scored.albumScore.entryName,
        "artist" -> scored.artistScore.entryName,
      )
  }
}
