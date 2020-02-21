package backend.external.expansions

import java.util.regex.Pattern

import backend.Url
import backend.external.{BaseLink, BaseLinks, Host}
import backend.external.expansions.WikipediaAlbumExternalLinksExpander._
import backend.logging.Logger
import backend.recon.Album
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Guice
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

import scalaz.Traverse
import scalaz.std.option.optionInstance
import scalaz.std.FutureInstances
import common.rich.func.{MoreTraversableInstances, MoreTraverseInstances, ToMoreFoldableOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._
import common.rich.RichT._

private class WikipediaAlbumExternalLinksExpander @Inject()(
    it: InternetTalker,
    logger: Logger,
    allMusicHelper: AllMusicHelper,
    expanderHelper: ExternalLinkExpanderHelper,
) extends ExternalLinkExpander[Album]
    // Use ops and instance traits since IntelliJ thinks the code doesn't compile and therefore will remove
    // ops and instance imports on optimization.
    with FutureInstances with MoreTraversableInstances with MoreTraverseInstances
    with ToMoreFoldableOps with ToMoreMonadErrorOps with ToTraverseMonadPlusOps {
  private implicit val iec: ExecutionContext = it
  override val sourceHost = Host.Wikipedia
  override val potentialHostsExtracted = Vector(Host.AllMusic)

  // semi-canonical = guaranteed to start with http://www.allmusic.com/album
  private def extractSemiCanonicalAllMusicLink(s: String): Option[String] = Option(s)
      .map(_.toLowerCase)
      .filter(_ contains "allmusic.com/album")
      // Canonize links, specifically the case where www. is missing, because trolls.
      .map(_.removeAll(AlbumPrefixPattern))
      .map("http://www.allmusic.com/album/" + _)

  /** Returns the first canonical link if one exists, otherwise returns the entire list */
  private def preferCanonical(xs: TraversableOnce[String]): TraversableOnce[String] =
    xs.find(allMusicHelper.isCanonical).mapHeadOrElse(List(_), xs)

  private def extractAllMusicLink(d: Document): Option[Url] = d
      .selectIterator("a")
      .map(_.href)
      .flatMap(extractSemiCanonicalAllMusicLink)
      .mapTo(preferCanonical)
      .map(Url)
      .mapTo(_.singleOpt)

  @VisibleForTesting
  def parseDocument(d: Document): BaseLinks[Album] =
    extractAllMusicLink(d).map(BaseLink[Album](_, Host.AllMusic))

  override def expand = expanderHelper(parseDocument)(_)
      // Compiler won't pick up type definitions, so explicitly naming Traverse is necessary
      .flatMap(Traverse[Traversable].traverse(_)(allMusicHelper.canonize))
      .flatMap(_.filterTraverse(link => allMusicHelper isValidLink link.link))
      .listenError(
        logger.error("WikipediaAlbumExternalLinksExpander failed to extract links", _))
      .orElse(Nil)
}

private object WikipediaAlbumExternalLinksExpander {
  import backend.module.CleanModule
  import net.codingwell.scalaguice.InjectorExtensions._

  import common.rich.RichFuture._

  def forUrl(path: String): BaseLink[Album] = new BaseLink[Album](Url(path), Host.Wikipedia)
  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector CleanModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[WikipediaAlbumExternalLinksExpander]
    $.expand(forUrl("""https://en.wikipedia.org/wiki/Ghost_(Devin_Townsend_Project_album)""")).get.log()
  }

  private val AlbumPrefixPattern = Pattern compile "^(https?://)?(www.)?allmusic.com/album/"
}
