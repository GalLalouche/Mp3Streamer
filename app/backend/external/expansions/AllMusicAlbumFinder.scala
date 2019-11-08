package backend.external.expansions

import backend.{FutureOption, Url}
import backend.external.Host
import backend.recon.Album
import backend.recon.ReconScorers.AlbumReconScorer
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

import scalaz.std.option.optionInstance
import scalaz.std.scalaFuture.futureInstance
import common.rich.func.MoreSeqInstances._
import common.rich.func.ToMoreMonadPlusOps._
import common.rich.func.ToTraverseMonadPlusOps._

import common.rich.RichT._
import common.rich.primitives.RichBoolean._

private class AllMusicAlbumFinder @Inject()(
    allMusicHelper: AllMusicHelper,
    sameHostExpanderHelper: SameHostExpanderHelper,
    ec: ExecutionContext
) extends SameHostExpander {
  private implicit val iec: ExecutionContext = ec

  override val host = Host.AllMusic

  private val documentToAlbumParser: DocumentToAlbumParser = new DocumentToAlbumParser {
    override def host: Host = AllMusicAlbumFinder.this.host

    override def modifyUrl(u: Url, a: Album) = u +/ "discography"
    override def findAlbum(d: Document, album: Album): FutureOption[Url] = {
      def score(other: Album): Double = AlbumReconScorer.apply(album, other)
      d.select(".discography table tbody tr").asScala.toSeq
          .tryMap(albumRow => albumRow ->
              Album(
                title = albumRow.select(".title").asScala.head.text,
                year = albumRow.select(".year").asScala.head.text.toInt,
                artist = album.artist)
          ).find(_._2.|>(score) >= 0.95)
          .map(_._1.select("td.title a").asScala
              .head
              .attr("href")
              .mapIf(_.startsWith("http").isFalse).to("http://www.allmusic.com" + _)
              .mapTo(Url))
          .filterTraverse(allMusicHelper.isValidLink)
    }
  }

  override def apply = sameHostExpanderHelper(documentToAlbumParser)
}
