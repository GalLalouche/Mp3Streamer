package backend.external.expansions

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scalaz.std.option.optionInstance
import scalaz.OptionT

import backend.external.Host
import backend.recon.{Album, AlbumReconScorer}
import backend.FutureOption
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreIteratorInstances._
import common.rich.func.ToMoreMonadPlusOps._
import common.rich.func.ToTraverseMonadPlusOps._
import common.rich.primitives.RichBoolean._
import common.rich.RichT._
import common.RichJsoup._
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

private class AllMusicAlbumFinder @Inject() (
    allMusicHelper: AllMusicHelper,
    sameHostExpanderHelper: SameHostExpanderHelper,
    ec: ExecutionContext,
    albumReconScorer: AlbumReconScorer,
) extends SameHostExpander {
  private implicit val iec: ExecutionContext = ec

  override val host = Host.AllMusic

  private val documentToAlbumParser: DocumentToAlbumParser = new DocumentToAlbumParser {
    override def host: Host = AllMusicAlbumFinder.this.host

    override def modifyUrl(u: Url, a: Album) = u.addPathPart("discography")
    override def findAlbum(d: Document, album: Album): FutureOption[Url] = OptionT {
      def score(other: Album): Double = albumReconScorer(album, other)
      d.selectIterator(".discography table tbody tr")
        .tryMap(albumRow =>
          albumRow ->
            Album(
              title = albumRow.selectSingle(".title").text,
              year = albumRow.selectSingle(".year").text.toInt,
              artist = album.artist,
            ),
        )
        .find(_._2.|>(score) >= 0.95)
        .map(
          _._1
            .selectFirst("td.title a")
            .href
            .mapIf(_.startsWith("http").isFalse)
            .to("http://www.allmusic.com" + _)
            .|>(Url.parse),
        )
        .filterM(allMusicHelper.isValidLink)
    }
  }

  override def apply = sameHostExpanderHelper(documentToAlbumParser)
}
