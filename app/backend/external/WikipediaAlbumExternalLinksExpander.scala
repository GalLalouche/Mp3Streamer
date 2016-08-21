package backend.external

import java.net.{HttpURLConnection, URL}
import java.util.regex.Pattern

import backend.Url
import backend.recon.Album
import common.RichFuture._
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

private class WikipediaAlbumExternalLinksExpander(implicit ec: ExecutionContext, interneter: InternetTalker)
    extends ExternalLinkExpander[Album](Host.Wikipedia) {
  private val canonicalLink = Pattern compile "[a-zA-Z\\-0-9]+-mw\\d+"
  private val canonicalRe = s"http://www.allmusic.com/album/($canonicalLink)".r
  private val nonCanonicalRe = "http://www.allmusic.com/album/(.*r\\d+)".r
  def canonize(e: ExternalLink[Album]): Future[ExternalLink[Album]] = {
    def aux(url: Url): Future[Url] =
      if (canonicalLink.matcher(url.address).matches)
        Future successful url
      else {
        val http = new URL(url.address).openConnection.asInstanceOf[HttpURLConnection]
        http.setInstanceFollowRedirects(false)
        interneter.connect(http)
            .filterWithMessage(_.getResponseCode == HttpURLConnection.HTTP_MOVED_PERM,
              e => s"Expected response code ${HttpURLConnection.HTTP_MOVED_PERM}, but was ${e.getResponseCode}")
            .map(_ getHeaderField "location")
            .map(Url)
      }
    aux(e.link).map(x => ExternalLink[Album](x, e.host))
  }
  //      } |> Url
  private def extractLink(s: String): Option[String] = {
    def extractUrl(r: Regex): Option[String] = {
      val $ = r.findAllIn(s)
      $.nonEmpty.ifTrue($ group 1)
    }
    extractUrl(canonicalRe).orElse(extractUrl(nonCanonicalRe))
  }
  private def preferCanonical(xs: Seq[String]): Seq[String] =
    xs.find(canonicalLink.matcher(_).matches)
        .map(List(_))
        .getOrElse(xs)

  override def aux(d: Document): Links[Album] = d
      .select("a")
      .map(_.attr("href"))
      .flatMap(extractLink)
      .mapTo(preferCanonical)
      .map(_
          .mapTo("http://www.allmusic.com/album/" + _)
          .mapTo(Url(_))
          .mapTo(url => ExternalLink[Album](url, Host("allmusic", url.host))))

  // explicitly changing Links to Traversable[ExternalLink[Album]] is needed for some reason
  override def apply(e: ExternalLink[Album]): Future[Traversable[ExternalLink[Album]]] =
    super.apply(e).flatMap(Future sequence _.map(canonize)).orElse(Nil)
}
