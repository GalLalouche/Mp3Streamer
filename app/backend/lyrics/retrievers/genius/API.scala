package backend.lyrics.retrievers.genius

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scalaz.OptionT

import backend.{FutureOption, Url => BackendUrl}
import backend.logging.Logger
import backend.lyrics.retrievers.genius.API._
import backend.recon.StringReconScorer
import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.RichT._
import io.lemonlabs.uri.Url
import models.Song
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

private class API @Inject() (
    @AccessToken accessToken: String,
    logger: Logger,
    it: InternetTalker,
) {
  private implicit val ec: ExecutionContext = it

  def getLyricUrl(song: Song): FutureOption[BackendUrl] = OptionT {
    val query = (split(song.artistName) ++ split(song.title)).mkString("+")
    it.get(Url(s"https://api.genius.com/search?access_token=$accessToken&q=$query"))
      .map(e =>
        if (e.status != Status.OK) {
          logger.info(s"Got status code <${e.status}> from genius\nMessage body\n: ${e.body}")
          None
        } else
          Json.parse(e.body).as[JsObject].|>(parse(song, _).map(BackendUrl.apply)),
      )
  }
}

private object API {
  import common.json.RichJson._

  private def split(s: String) = s.split(" ")
  @VisibleForTesting def parse(song: Song, json: JsObject): Option[String] = for {
    firstHit <- (json / "response").objects("hits").headOption
    result = firstHit / "result"
    artist = (result / "primary_artist").str("name")
    title = result.str("title")
    if StringReconScorer(artist, song.artistName) >= 0.9 && StringReconScorer(
      title,
      song.title,
    ) >= 0.9
  } yield result.str("url")
}
