package backend.external

import java.util.regex.Pattern

import backend.Url
import backend.recon.Album
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._

object WikipediaAlbumExternalLinksExpander extends ExternalLinkExpander[Album](Host.Wikipedia) {
  val re = Pattern compile "http://www.allmusic.com/album/[a-zA-Z\\-0-9]+"
  private def extractAllMusicLink(d: Document): ExternalLink[Album] =
    d.select("a")
        .map(_.attr("href"))
        .filter(re.matcher(_).matches)
        .single
        .mapTo(Url)
        .mapTo(url => ExternalLink(url, Host("allmusic", url.host)))

  override def apply(d: Document): Links[Album] = {
    List(extractAllMusicLink(d))
  }
}
