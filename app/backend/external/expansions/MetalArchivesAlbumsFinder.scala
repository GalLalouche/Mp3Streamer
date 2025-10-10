package backend.external.expansions

import backend.FutureOption
import backend.external.Host
import backend.recon.Album
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

import common.rich.func.kats.MoreFutureInstances.futureIsPure
import common.rich.func.kats.ToTransableOps.toHoistIdOps

import common.RichJsoup._

private class MetalArchivesAlbumsFinder @Inject() (sameHostExpanderHelper: SameHostExpanderHelper)
    extends SameHostExpander {
  override val host: Host = Host.MetalArchives
  override val qualityRank = 1
  private val documentToAlbumParser: DocumentToAlbumParser = new DocumentToAlbumParser {
    override val host = MetalArchivesAlbumsFinder.this.host

    override def modifyUrl(u: Url, a: Album) = {
      val address = u.toStringPunycode
      val albumArtistName = a.artist.name
      val urlArtistName = address.split('/').dropRight(1).last
      val artistId = address.split('/').last.toInt
      Url.parse(s"http://www.metal-archives.com/band/discography/id/$artistId/tab/all")
    }
    override def findAlbum(d: Document, a: Album): FutureOption[Url] =
      d.selectIterator(".display.discog tr td a")
        .find(_.text.equalsIgnoreCase(a.title))
        .map(_.href)
        .map(Url.parse)
        .hoistId
  }

  override def apply = sameHostExpanderHelper.apply(documentToAlbumParser)
}
