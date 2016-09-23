package backend.external.expansions

import backend.Url
import backend.configs.CleanConfiguration
import backend.external.Host.AllMusic
import backend.external._
import backend.recon.Album
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.MoreTraverse._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.{ToMonadOps, ToTraverseOps}

private class WikipediaAlbumExternalLinksExpander(implicit ec: ExecutionContext, it: InternetTalker)
    extends ExternalLinkExpanderTemplate[Album](Host.Wikipedia, List(Host.AllMusic))
        with FutureInstances with ToMonadOps with ToTraverseOps {
  protected val allMusicHelper = new AllMusicHelper
  def canonize(e: ExternalLink[Album]): Future[ExternalLink[Album]] =
    if (e.host == AllMusic)
      allMusicHelper.canonize(e)
    else
      Future successful e

  // semi-canonical = guaranteed to start with http://www.allmusic.com/album
  private def extractSemiCanonicalAllMusicLink(s: String): Option[String] = Option(s)
      .map(_.toLowerCase)
      .filter(_ contains "allmusic.com/album")
      // Canonize links, specifically the case where www. is missing, because trolls.
      .map(_.replaceFirst("^(https?://)?(www.)?allmusic.com/album/", "")).map("http://www.allmusic.com/album/" + _)

  /** Returns the first canonical link if one exists, otherwise returns the entire list */
  private def preferCanonical(xs: Seq[String]): Seq[String] = xs
      .find(allMusicHelper.isCanonical)
      .map(List(_))
      .getOrElse(xs)

  private def extractAllMusicLink(d: Document): Option[Url] = d
      .select("a")
      .map(_.attr("href"))
      .flatMap(extractSemiCanonicalAllMusicLink)
      .mapTo(preferCanonical)
      .map(_.mapTo(Url))
      .mapTo(us => if (us.size <= 1) us.headOption else throw new IllegalStateException("extracted too many AllMusic links"))

  override def parseDocument(d: Document): Links[Album] =
    extractAllMusicLink(d).map(ExternalLink[Album](_, Host.AllMusic))

  // TODO move this to somewhere more common
  private def filterFutures[T](fs: Traversable[T], p: T => Future[Boolean]): Future[Traversable[T]] =
    fs.traverse(e => p(e).map(e -> _)).map(_.filter(_._2).map(_._1))
  override def apply(e: ExternalLink[Album]) = super.apply(e)
      // explicitly changing Links to Traversable is needed for some reason
      .flatMap(_.toTraversable.traverse(allMusicHelper.canonize))
      .flatMap(links => filterFutures[ExternalLink[Album]](links, link => allMusicHelper.isValidLink(link.link)))
      .orElse(Nil)
}

private object WikipediaAlbumExternalLinksExpander {
  def forUrl(path: String): ExternalLink[Album] = new ExternalLink[Album](Url(path), Host.Wikipedia)
  def main(args: Array[String]): Unit = {
    implicit val c = CleanConfiguration
    val $ = new WikipediaAlbumExternalLinksExpander()
    $.apply(forUrl("""https://en.wikipedia.org/wiki/Ghost_(Devin_Townsend_Project_album)""")).get.log()
  }
}
