package backend.lyrics.retrievers.genius

import backend.{FutureOption, Url}
import backend.lyrics.retrievers.genius.API._
import backend.recon.StringReconScorer
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext

import common.io.InternetTalker
import common.rich.RichT._

private class API @Inject()(
    @AccessToken accessToken: String,
    it: InternetTalker,
) {
  private implicit val ec: ExecutionContext = it

  def getLyricUrl(song: Song): FutureOption[Url] = {
    val query = split(song.artistName) ++ split(song.title) mkString "+"
    it.get(Url(s"https://api.genius.com/search?access_token=$accessToken&q=$query"))
        .map(_.optFilter(_.status == Status.OK)
            .map(Json parse _.body)
            .map(_.as[JsObject])
            .flatMap(parse(song, _))
            .map(Url)
        )
  }
}

private object API {
  import common.json.RichJson._

  private def split(s: String) = s split " "
  @VisibleForTesting def parse(song: Song, json: JsObject): Option[String] = for {
    firstHit <- (json / "response" objects "hits").headOption
    result = firstHit / "result"
    artist = result / "primary_artist" str "name"
    title = result str "title"
    if StringReconScorer(artist, song.artistName) >= 0.9 && StringReconScorer(title, song.title) >= 0.9
  } yield result str "url"
}