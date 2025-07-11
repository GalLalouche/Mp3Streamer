package backend.external.expansions

import backend.FutureOption
import backend.external.Host
import backend.external.expansions.WikipediaAlbumFinder.SupportedLanguages
import backend.recon.{Album, StringReconScorer}
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToTransableOps.{toHoistIdOps, toUnrunOps}
import common.rich.func.ToTraverseMonadPlusOps._
import scalaz.std.vector.vectorInstance

import common.RichJsoup._
import common.RichUrl.richUrl
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.primitives.RichString._

/** Finds Wikipedia album links in an artist's Wikipedia page. */
private class WikipediaAlbumFinder @Inject() (
    sameHostExpanderHelper: SameHostExpanderHelper,
    it: InternetTalker,
    stringReconScorer: StringReconScorer,
    ec: ExecutionContext,
) extends SameHostExpander {
  private implicit val iec: ExecutionContext = ec
  override val host = Host.Wikipedia
  override val qualityRank = 2 // Parsing wikipedia for links is pretty error-prone.

  private val documentToAlbumParser: DocumentToAlbumParser = new DocumentToAlbumParser {
    override def host: Host = WikipediaAlbumFinder.this.host

    private def isNotRedirected(lang: String)(link: Url): Future[Boolean] = {
      // TODO Should check if the redirection points to the original url.
      val title = link.toStringPunycode.takeAfterLast('/')
      assert(title.nonEmpty)
      val urlWithoutRedirection =
        s"https://$lang.wikipedia.org/w/index.php?title=$title&redirect=no"
      val url = Url.parse(urlWithoutRedirection)
      it.downloadDocument(url)
        .map(_.find("span#redirectsub").forall(_.text != "Redirect page"))
    }
    def findAlbum(d: Document, a: Album): FutureOption[Url] = {
      def score(linkName: String): Double = stringReconScorer(a.title, linkName)
      val lang = d.selectSingle("html").attr("lang")
      for {
        documentLanguage <- lang.optFilter(SupportedLanguages).hoistId
        res <- d
          .selectIterator("a")
          .filter(e => score(e.text) > 0.95)
          .map(_.href)
          .filter(_.nonEmpty)
          // Remove external links, see https://stackoverflow.com/q/4071117/736508
          .filterNot(_ contains "//")
          .filterNot(_ contains "redlink=1")
          .filterNot(_ contains "File:")
          .map(s"https://$documentLanguage.wikipedia.org" + _ |> Url.parse)
          .|>(_.toVector.ensuring(_.forall(_.isValid)))
          .filterM(isNotRedirected(documentLanguage))
          .map(_.headOption)
          .unrun
      } yield res
    }
  }

  override def apply = sameHostExpanderHelper(documentToAlbumParser)
}

private object WikipediaAlbumFinder {
  private val SupportedLanguages = Set("en", "he")
}
