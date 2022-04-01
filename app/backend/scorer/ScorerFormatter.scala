package backend.scorer

import backend.scorer.ModelScorer.SongScore
import controllers.UrlPathUtils
import javax.inject.Inject
import models.Song
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.JsonWriteable
import common.json.ToJsonableOps._

private object ScorerFormatter {
  private implicit object SongScoreJsonable extends JsonWriteable[SongScore] {
    override def jsonify(a: SongScore) = a match {
      case SongScore.Default => Json.obj()
      case SongScore.Scored(score, source, song, album, artist) => Json.obj(
        "score" -> score.entryName,
        "source" -> source.toString,
        "song" -> song.orDefaultString,
        "album" -> album.orDefaultString,
        "artist" -> artist.orDefaultString,
      )
    }
  }
}

/** Fetches and updates scores for songs, albums, and artists. */
private class ScorerFormatter @Inject()(
    modelScorer: ModelScorer,
    urlPathUtils: UrlPathUtils,
    ec: ExecutionContext,
) {
  import ScorerFormatter._

  private implicit val iec: ExecutionContext = ec
  def getScore(filePath: String): Future[JsValue] =
    modelScorer(urlPathUtils.parseSong(filePath)).map(_.jsonify)
  private def update(f: (Song, ModelScore) => Future[Unit], filePath: String, score: String) = f(
    urlPathUtils.parseSong(filePath),
    ModelScore.withNameInsensitive(score),
  )
  def updateSongScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateSongScore, filePath, score)
  def updateAlbumScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateAlbumScore, filePath, score)
  def updateArtistScore(filePath: String, score: String): Future[Unit] =
    update(modelScorer.updateArtistScore, filePath, score)
}
