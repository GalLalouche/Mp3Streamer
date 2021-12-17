package backend.scorer

import backend.recon.Reconcilable._
import backend.scorer.ModelScorer.SongScore
import controllers.UrlPathUtils
import javax.inject.Inject
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.{ExecutionContext, Future}

import common.json.JsonWriteable
import common.json.ToJsonableOps._

private object ScorerFormatter {
  private implicit object SongScoreJsonable extends JsonWriteable[SongScore] {
    override def jsonify(a: SongScore) = a match {
      case SongScore.Default => Json.obj(
        "score" -> ModelScore.Default.entryName,
      )
      case SongScore.Scored(score, source) => Json.obj(
        "score" -> score.entryName,
        "source" -> source.toString,
      )
    }
  }
}

// TODO integration test
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
  def updateSongScore(filePath: String, score: String): Future[Unit] =
    modelScorer.updateSongScore(
      urlPathUtils.parseSong(filePath),
      ModelScore.withNameInsensitive(score),
    )
  def updateAlbumScore(filePath: String, score: String): Future[Unit] =
    modelScorer.updateAlbumScore(
      urlPathUtils.parseSong(filePath).release,
      ModelScore.withNameInsensitive(score),
    )
  def updateArtistScore(filePath: String, score: String): Future[Unit] =
    modelScorer.updateArtistScore(
      urlPathUtils.parseSong(filePath).artist,
      ModelScore.withNameInsensitive(score),
    )
}
