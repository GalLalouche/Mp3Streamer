package backend.lyrics.retrievers.genius

import java.net.HttpURLConnection

import backend.FutureOption
import backend.lyrics.retrievers.genius.API._
import backend.recon.StringReconScorer
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext

import cats.data.OptionT

import common.io.InternetTalker
import common.rich.RichT._

private class API @Inject() (
    @AccessToken accessToken: String,
    it: InternetTalker,
    scorer: StringReconScorer,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def getLyricUrl(song: Song): FutureOption[Url] = OptionT {
    val query = (split(song.artistName) ++ split(song.title)).mkString("+")
    it.get(Url(s"https://api.genius.com/search?access_token=$accessToken&q=$query"))
      .map(e =>
        if (e.status != HttpURLConnection.HTTP_OK) {
          scribe.info(s"Got status code <${e.status}> from genius\nMessage body\n: ${e.body}")
          None
        } else
          Json.parse(e.body).as[JsObject].|>(parse(song, _, scorer).map(Url.parse)),
      )
  }
}

private object API {
  import common.json.RichJson._

  private def split(s: String) = s.split(" ")
  @VisibleForTesting def parse(
      song: Song,
      json: JsObject,
      scorer: StringReconScorer,
  ): Option[String] = for {
    firstHit <- (json / "response").objects("hits").headOption
    result = firstHit / "result"
    artist = (result / "primary_artist").str("name")
    title = result.str("title")
    if scorer(artist, song.artistName) >= 0.9 && scorer(title, song.title) >= 0.9
  } yield result.str("url")
}
