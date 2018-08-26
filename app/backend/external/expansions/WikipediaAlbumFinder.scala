package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.{Album, StringReconScorer}
import common.io.InternetTalker
import common.rich.primitives.RichBoolean._
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, MoreTraverseInstances, ToTraverseMonadPlusOps}
import common.rich.primitives.RichString._
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

private class WikipediaAlbumFinder @Inject()(ec: ExecutionContext, it: InternetTalker)
    extends SameHostExpander(Host.Wikipedia, ec, it)
        with ToTraverseMonadPlusOps with FutureInstances with MoreSeqInstances with MoreTraverseInstances {
  private implicit val iec: ExecutionContext = ec
  override protected def findAlbum(d: Document, a: Album): Future[Option[Url]] = {
    def score(linkName: String): Double = StringReconScorer(a.title, linkName)
    d.select("a").asScala.toSeq
        .filter(e => score(e.text) > 0.95)
        .map(_.attr("href"))
        .filter(_.nonEmpty)
        .filterNot(_ contains "redlink=1")
        .map("https://en.wikipedia.org" + _ |> Url)
        .filterTraverse(isNotRedirected)
        .map(_.headOption)
  }

  private def isNotRedirected(link: Url): Future[Boolean] = {
    // TODO Should check if the redirection points to the original url.
    val title = link.address dropAfterLast '/'
    val urlWithoutRedirection = s"https://en.wikipedia.org/w/index.php?title=$title&redirect=no"
    it.downloadDocument(Url(urlWithoutRedirection))
        .map(_.select("span#redirectsub").asScala.headOption.exists(_.text == "Redirect page").isFalse)
  }
}
