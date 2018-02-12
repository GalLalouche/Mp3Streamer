package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import common.rich.primitives.RichBoolean._
import backend.recon.{Album, StringReconScorer}
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.func.ToMoreMonadOps
import common.rich.primitives.RichString._
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scalaz.std.FutureInstances

private class WikipediaAlbumFinder(implicit it: InternetTalker) extends SameHostExpander(Host.Wikipedia)
    with FutureInstances with ToMoreMonadOps {
  override protected def findAlbum(d: Document, a: Album): Option[Url] = {
    def score(linkName: String): Double = StringReconScorer(a.title, linkName)
    d.select("a").asScala
        .find(e => score(e.text) > 0.95)
        .map(_.attr("href"))
        .filter(_.nonEmpty)
        .filterNot(_ contains "redlink=1")
        .map("https://en.wikipedia.org" + _ |> Url)
  }

  private def isNotRedirected(link: Url): Future[Boolean] = {
    val title = link.address dropAfterLast '/'
    val urlWithoutRedirection = s"https://en.wikipedia.org/w/index.php?title=$title&redirect=no"
    it.downloadDocument(Url(urlWithoutRedirection))
        .map(_.select("span#redirectsub").asScala.headOption.exists(_.text == "Redirect page").isFalse)
  }

  override protected def fromUrl(u: Url, a: Album): Future[Option[BaseLink[Album]]] =
    super.fromUrl(u, a).mFilterOpt(_.link |> isNotRedirected)
}
