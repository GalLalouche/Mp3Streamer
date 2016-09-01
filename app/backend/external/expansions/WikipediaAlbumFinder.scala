package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.{Album, StringReconScorer}
import common.io.InternetTalker
import scala.collection.JavaConversions._
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext
import common.rich.RichT._

private class WikipediaAlbumFinder(implicit ec: ExecutionContext) extends SameHostExpander(Host.Wikipedia) {
  override def aux(d: Document, a: Album): Option[Url] = {
    def score(linkName: String): Double = StringReconScorer(a.title, linkName)
    d.select("a")
        .find(e => score(e.text) > 0.95)
        .map(_.attr("href"))
        .filter(_.nonEmpty)
        .map("https://en.wikipedia.org" + _ |> Url)
  }
}