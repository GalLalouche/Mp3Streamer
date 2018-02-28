package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import backend.logging.LoggerProvider
import backend.recon.{Album, Artist}
import backend.recon.ReconScorers.AlbumReconScorer
import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, ToMoreMonadPlusOps, ToTraverseMonadPlusOps}
import common.rich.primitives.RichBoolean._
import org.jsoup.nodes.Document
import scalaz.std.{FutureInstances, OptionInstances}

import scala.collection.JavaConverters._
import scala.concurrent.Future

private class AllMusicAlbumFinder(allMusicHelper: AllMusicHelper)(implicit it: InternetTalker, lp: LoggerProvider)
    extends SameHostExpander(Host.AllMusic)
        with ToMoreMonadPlusOps with ToTraverseMonadPlusOps
        with MoreSeqInstances with OptionInstances with FutureInstances {
  @VisibleForTesting
  private[expansions] def this()(implicit it: InternetTalker, lp: LoggerProvider) = this(new AllMusicHelper)

  override protected def findAlbum(d: Document, album: Album): Future[Option[Url]] = {
    def score(other: Album): Double = AlbumReconScorer.apply(album, other)
    d.select(".discography table tbody tr").asScala.toSeq
        .tryMap(albumRow => albumRow ->
            Album(
              title = albumRow.select(".title").asScala.head.text,
              year = albumRow.select(".year").asScala.head.text.toInt,
              artist = album.artist)
        ).find(_._2.|>(score) >= 0.95)
        .map(_._1.select("td a").asScala
            .head
            .attr("href")
            .mapIf(_.startsWith("http").isFalse).to("http://www.allmusic.com" + _)
            .mapTo(Url))
        .filterTraverse(allMusicHelper.isValidLink)
  }

  override protected def fromUrl(u: Url, a: Album): Future[Option[BaseLink[Album]]] =
    super.fromUrl(u +/ "discography", a)
}
