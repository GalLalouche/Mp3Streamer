package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.Album
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.Future

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
    override def findAlbum(d: Document, a: Album): Future[Option[Url]] =
      Future.successful(d.select(".display.discog tr td a").asScala
          .find(_.text.toLowerCase == a.title.toLowerCase)
          .map(_.attr("href"))
          .map(Url))
  }

  override def apply = sameHostExpanderHelper.apply(documentToAlbumParser)
}
