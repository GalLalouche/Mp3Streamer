package backend.scorer

import java.io.File

import com.google.inject.Inject
import models.{IOSongTagParser, Song}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.JsonWriteable
import common.json.ToJsonableOps.jsonifySingle

/** Fetches and updates scores for songs, albums, and artists. */
class ScorerFormatter @Inject() (modelScorer: FullInfoModelScorer, ec: ExecutionContext) {
  import ScorerFormatter._
  private implicit val iec: ExecutionContext = ec
  def getScore(filePath: String): Future[JsValue] =
    modelScorer(IOSongTagParser(new File(filePath))).map(_.jsonify)
  def openScoreFile(filePath: String): Future[Unit] = modelScorer.openScoreFile(toSong(filePath))
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
