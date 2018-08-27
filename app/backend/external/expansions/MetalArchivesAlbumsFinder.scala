package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.Album
import common.io.InternetTalker
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

private class MetalArchivesAlbumsFinder @Inject()(
    ec: ExecutionContext,
    it: InternetTalker,
) extends SameHostExpander(Host.MetalArchives, ec, it) {
  private implicit val iec: ExecutionContext = ec
  override protected def findAlbum(d: Document, a: Album): Future[Option[Url]] =
    Future.successful(d.select(".display.discog tr td a").asScala
        .find(_.text.toLowerCase == a.title.toLowerCase)
        .map(_.attr("href"))
        .map(Url))

  override def fromUrl(u: Url, a: Album) = {
    val address = u.address
    val albumArtistName = a.artist.name
    val urlArtistName = address.split('/').dropRight(1).last
    val artistId = address.split('/').last.toInt
    super.fromUrl(Url(s"http://www.metal-archives.com/band/discography/id/$artistId/tab/all"), a)
  }
}
