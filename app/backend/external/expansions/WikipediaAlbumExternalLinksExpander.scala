package backend.external.expansions

import java.util.regex.Pattern
import javax.inject.Inject

import backend.external.{BaseLink, BaseLinks, Host}
import backend.external.expansions.WikipediaAlbumExternalLinksExpander._
import backend.logging.Logger
import backend.recon.Album
import com.google.common.annotations.VisibleForTesting
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

import common.rich.func.{MoreTraversableInstances, MoreTraverseInstances, ToMoreFoldableOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import common.rich.func.BetterFutureInstances._
import scalaz.Traverse
import scalaz.std.option.optionInstance

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._

private class WikipediaAlbumExternalLinksExpander @Inject() (
    it: InternetTalker,
    logger: Logger,
    allMusicHelper: AllMusicHelper,
    expanderHelper: ExternalLinkExpanderHelper,
) extends ExternalLinkExpander[Album]
    // Use ops and instance traits since IntelliJ thinks the code doesn't compile and therefore will remove
    // ops and instance imports on optimization.
    with MoreTraversableInstances
    with MoreTraverseInstances
    with ToMoreFoldableOps
    with ToMoreMonadErrorOps
    with ToTraverseMonadPlusOps {
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
    xs.find(allMusicHelper.isCanonical).mapHeadOrElse(Vector(_), xs)

  private def extractAllMusicLink(d: Document): Option[Url] = d
    .selectIterator("a")
    .map(_.href)
    .flatMap(extractSemiCanonicalAllMusicLink)
    .|>(preferCanonical)
    .map(Url.parse)
    .|>(_.singleOpt)

  @VisibleForTesting
  def parseDocument(d: Document): BaseLinks[Album] =
    extractAllMusicLink(d).map(BaseLink[Album](_, Host.AllMusic))

  override def expand = expanderHelper(parseDocument)(_)
    // Compiler won't pick up type definitions, so explicitly naming Traverse is necessary
    .flatMap(Traverse[Traversable].traverse(_)(allMusicHelper.canonize))
    .flatMap(_.filterM(allMusicHelper isValidLink _.link))
    .listenError(logger.error("WikipediaAlbumExternalLinksExpander failed to extract links", _))
    .orElse(Nil)
}

private object WikipediaAlbumExternalLinksExpander {
  private val AlbumPrefixPattern = Pattern.compile("^(https?://)?(www.)?allmusic.com/album/")
}
