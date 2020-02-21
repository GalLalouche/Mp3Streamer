package backend.external.expansions

import backend.{FutureOption, Url}
import backend.external.Host
import backend.recon.Album
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.concurrent.Future

import common.RichJsoup._

private class MetalArchivesAlbumsFinder @Inject()(sameHostExpanderHelper: SameHostExpanderHelper)
    extends SameHostExpander {
  override val host: Host = Host.MetalArchives
  private val documentToAlbumParser: DocumentToAlbumParser = new DocumentToAlbumParser {
    override val host = MetalArchivesAlbumsFinder.this.host

    override def modifyUrl(u: Url, a: Album) = {
      val address = u.address
      val albumArtistName = a.artist.name
      val urlArtistName = address.split('/').dropRight(1).last
      val artistId = address.split('/').last.toInt
      Url(s"http://www.metal-archives.com/band/discography/id/$artistId/tab/all")
    }
    override def findAlbum(d: Document, a: Album): FutureOption[Url] =
      Future.successful(d.selectIterator(".display.discog tr td a")
          .find(_.text.toLowerCase == a.title.toLowerCase)
          .map(_.href)
          .map(Url))
  }

  override def apply = sameHostExpanderHelper.apply(documentToAlbumParser)
}
