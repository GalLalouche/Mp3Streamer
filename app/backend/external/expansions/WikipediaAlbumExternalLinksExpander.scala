package backend.external.expansions

import backend.Url
import backend.configs.{CleanConfiguration, Configuration}
import backend.external._
import backend.logging.Logger
import backend.recon.Album
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.func._
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

import scalaz.Traverse
import scalaz.std.{FutureInstances, OptionInstances}

private class WikipediaAlbumExternalLinksExpander(implicit c: Configuration)
    extends ExternalLinkExpanderTemplate[Album](Host.Wikipedia, List(Host.AllMusic))
        with MoreTraversableInstances with ToTraverseMonadPlusOps with ToMoreMonadErrorOps
        with ToMoreFoldableOps with FutureInstances with OptionInstances with MoreTraverseInstances {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val logger = c.injector.instance[Logger]
  protected val allMusicHelper = c.injector.instance[AllMusicHelper]

  // semi-canonical = guaranteed to start with http://www.allmusic.com/album
  private def extractSemiCanonicalAllMusicLink(s: String): Option[String] = Option(s)
      .map(_.toLowerCase)
      .filter(_ contains "allmusic.com/album")
      // Canonize links, specifically the case where www. is missing, because trolls.
      .map(_.replaceFirst("^(https?://)?(www.)?allmusic.com/album/", ""))
      .map("http://www.allmusic.com/album/" + _)

  /** Returns the first canonical link if one exists, otherwise returns the entire list */
  private def preferCanonical(xs: Seq[String]): Seq[String] =
    xs.find(allMusicHelper.isCanonical).mapHeadOrElse(List(_), xs)

  private def extractAllMusicLink(d: Document): Option[Url] = d
      .select("a").asScala
      .map(_.attr("href"))
      .flatMap(extractSemiCanonicalAllMusicLink)
      .mapTo(preferCanonical)
      .map(Url)
      .mapTo(urls =>
        if (urls hasAtMostSizeOf 1) urls.headOption
        else throw new IllegalStateException("extracted too many AllMusic links"))

  override def parseDocument(d: Document): BaseLinks[Album] =
    extractAllMusicLink(d).map(BaseLink[Album](_, Host.AllMusic))

  override def apply(e: BaseLink[Album]) = super.apply(e)
      // Compiler won't pick up type definitions, so explicitly naming Traverse is necessary
      .flatMap(Traverse[Traversable].traverse(_)(allMusicHelper.canonize))
      .flatMap(_.filterTraverse(link => allMusicHelper isValidLink link.link))
      .listenError(
        logger.error("WikipediaAlbumExternalLinksExpander failed to extract links", _))
      .orElse(Nil)
}

private object WikipediaAlbumExternalLinksExpander {
  def forUrl(path: String): BaseLink[Album] = new BaseLink[Album](Url(path), Host.Wikipedia)
  def main(args: Array[String]): Unit = {
    implicit val c: Configuration = CleanConfiguration
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    val $ = new WikipediaAlbumExternalLinksExpander()
    $.apply(forUrl("""https://en.wikipedia.org/wiki/Ghost_(Devin_Townsend_Project_album)""")).get.log()
  }
}
