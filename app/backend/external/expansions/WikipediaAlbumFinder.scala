package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.{Album, StringReconScorer}
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.func.ToTraverseMonadPlusOps
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import org.jsoup.nodes.Document
import scalaz.std.{FutureInstances, OptionInstances}

import scala.collection.JavaConverters._
import scala.concurrent.Future

private class WikipediaAlbumFinder(implicit it: InternetTalker) extends SameHostExpander(Host.Wikipedia)
    with ToTraverseMonadPlusOps with FutureInstances with OptionInstances {
  override protected def findAlbum(d: Document, a: Album): Future[Option[Url]] = {
    def score(linkName: String): Double = StringReconScorer(a.title, linkName)
    d.select("a").asScala
        .find(e => score(e.text) > 0.95)
        .map(_.attr("href"))
        .filter(_.nonEmpty)
        .filterNot(_ contains "redlink=1")
        .map("https://en.wikipedia.org" + _ |> Url)
        .filterTraverse(isNotRedirected)
  }

  private def isNotRedirected(link: Url): Future[Boolean] = {
    val title = link.address dropAfterLast '/'
    val urlWithoutRedirection = s"https://en.wikipedia.org/w/index.php?title=$title&redirect=no"
    it.downloadDocument(Url(urlWithoutRedirection))
        .map(_.select("span#redirectsub").asScala.headOption.exists(_.text == "Redirect page").isFalse)
  }
}
