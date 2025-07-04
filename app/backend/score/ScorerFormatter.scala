package backend.score

import java.io.File

import backend.score.ScorerFormatter.toSong
import com.google.inject.Inject
import models.{IOSongTagParser, Song}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.JsonWriteable
import common.json.ToJsonableOps.jsonifySingle

/** Fetches and updates scores for songs, albums, and artists. */
class ScorerFormatter @Inject() ($ : ScorerModel, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  import ScorerFormatter.songScoreJsonable
  def getScore(filePath: String): Future[JsValue] =
    $(IOSongTagParser(new File(filePath))).map(_.jsonify)

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

private object ScorerFormatter {
  private def toSong(path: String): Song = IOSongTagParser(new File(path))

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
