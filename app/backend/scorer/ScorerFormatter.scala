package backend.scorer

import java.io.File
import com.google.inject.Inject

import models.{IOSongTagParser, Song}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.JsonWriteable
import common.json.ToJsonableOps._

/** Fetches and updates scores for songs, albums, and artists. */
class ScorerFormatter @Inject() ($ : ScorerModel, ec: ExecutionContext) {
  import ScorerFormatter.SongScoreJsonable
  private implicit val iec: ExecutionContext = ec
  def getScore(filePath: String): Future[JsValue] =
    $(IOSongTagParser(new File(filePath))).map(_.jsonify)
  private def update(
      f: (Song, OptionalModelScore) => Future[Unit],
      filePath: String,
      score: String,
  ) = f(IOSongTagParser(new File(filePath)), OptionalModelScore.withNameInsensitive(score))
  def updateSongScore(filePath: String, score: String): Future[Unit] =
    update($.updateSongScore, filePath, score)
  def updateAlbumScore(filePath: String, score: String): Future[Unit] =
    update($.updateAlbumScore, filePath, score)
  def updateArtistScore(filePath: String, score: String): Future[Unit] =
    update($.updateArtistScore, filePath, score)
}

private object ScorerFormatter {
  private implicit val SongScoreJsonable: JsonWriteable[FullInfoScore] = {
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
