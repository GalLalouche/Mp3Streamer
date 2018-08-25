package backend.external.expansions

import backend.Url
import backend.configs.{Configuration, StandaloneConfig}
import backend.external.Host
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

private class MetalArchivesAlbumsFinder(implicit c: Configuration) extends SameHostExpander(Host.MetalArchives) {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
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
private object MetalArchivesAlbumsFinder {
  def main(args: Array[String]) {
    implicit val c: Configuration = StandaloneConfig
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    new MetalArchivesAlbumsFinder().fromUrl(Url("http://www.metal-archives.com/bands/Empyrium/2345"),
      Album("The turn of the tides", 2014, Artist("Empyrium"))).get.log()
  }
}
