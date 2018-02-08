package backend.external.expansions

import backend.Url
import backend.configs.StandaloneConfig
import backend.external.Host
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

private class MetalArchivesAlbumsFinder(implicit it: InternetTalker) extends SameHostExpander(Host.MetalArchives) {
  override protected def findAlbum(d: Document, a: Album): Option[Url] =
    d.select(".display.discog tr td a").asScala
        .find(_.text.toLowerCase == a.title.toLowerCase)
        .map(_.attr("href"))
        .map(Url)

  override def fromUrl(u: Url, a: Album) = {
    val address = u.address
    val albumArtistName = a.artist.name
    val urlArtistName = address.split('/').dropRight(1).last
    val artistId = address.split('/').last.toInt
    super.fromUrl(Url(s"http://www.metal-archives.com/band/discography/id/$artistId/tab/all"), a)
  }
}
private object MetalArchivesAlbumsFinder {
  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    new MetalArchivesAlbumsFinder().fromUrl(Url("http://www.metal-archives.com/bands/Empyrium/2345"),
      Album("The turn of the tides", 2014, Artist("Empyrium"))).get.log()
  }
}
