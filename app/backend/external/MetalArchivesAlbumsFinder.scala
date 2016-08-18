package backend.external

import backend.recon.{Album, Artist}
import backend.{StandaloneConfig, Url}
import common.RichFuture._
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

private class MetalArchivesAlbumsFinder(implicit ec: ExecutionContext) extends SameHostExpander(Host.MetalArchives) {
  override def aux(d: Document, a: Album): Option[Url] =
    d.select(".display.discog tr td a")
        .find(_.text.toLowerCase == a.title.toLowerCase)
        .map(_.attr("href"))
        .map(Url)

  override def fromUrl(u: Url, a: Album) = {
    val address = u.address
    val albumArtistName = a.artist.name
    val urlArtistName = address.split('/').dropRight(1).last
    require(urlArtistName == albumArtistName,
      s"Wrong band name; expected $albumArtistName but was $urlArtistName")
    val artistId = address.split('/').last.toInt
    super.fromUrl(Url(s"http://www.metal-archives.com/band/discography/id/$artistId/tab/all"), a)
  }
}

object MetalArchivesAlbumsFinder {
  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    new MetalArchivesAlbumsFinder().fromUrl(Url("http://www.metal-archives.com/bands/Cruachan/86"),
      Album("Blood for the Blood God", Artist("Cruachan"))).get.log()
  }
}
