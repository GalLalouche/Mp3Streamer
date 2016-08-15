package backend.external

import backend.Url
import backend.recon.Album
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import common.rich.primitives.RichBoolean._

private class WikipediaAlbumExternalLinksExpander(implicit ec: ExecutionContext) extends ExternalLinkExpander[Album](Host.Wikipedia) {
  private val re = "http://www.allmusic.com/album/([a-zA-Z\\-0-9]+)".r

  private def extractLink(s: String): Option[String] = {
    val $ = re.findAllIn(s)
    ($.nonEmpty && s.contains("-")).ifTrue($ group 1)
  }

  override def aux(d: Document): Links[Album] = d
      .select("a")
      .map(_.attr("href"))
      .flatMap(extractLink)
      .filter(_ contains "-")
      .map(_
          .mapTo("http://www.allmusic.com/album/" + _)
          .mapTo(Url(_))
          .mapTo(url => ExternalLink[Album](url, Host("allmusic", url.host))))
}
