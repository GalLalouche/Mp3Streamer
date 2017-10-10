package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.{Album, StringReconScorer}
import common.io.InternetTalker
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

private class WikipediaAlbumFinder(implicit it: InternetTalker) extends SameHostExpander(Host.Wikipedia) {
  override def findAlbum(d: Document, a: Album): Option[Url] = {
    def score(linkName: String): Double = StringReconScorer(a.title, linkName)
    d.select("a").asScala
        .find(e => score(e.text) > 0.95)
        .map(_.attr("href"))
        .filter(_.nonEmpty)
        .filterNot(_ contains "redlink=1")
        .map("https://en.wikipedia.org" + _ |> Url)
  }
}
