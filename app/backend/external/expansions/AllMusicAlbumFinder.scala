package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import backend.recon.ReconScorers.AlbumReconScorer
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.func.ToMoreMonadOps
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.Try
import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.ToTraverseOps

private class AllMusicAlbumFinder(implicit it: InternetTalker) extends SameHostExpander(Host.AllMusic)
    with FutureInstances with OptionInstances with ToTraverseOps with ToMoreMonadOps {
  val allMusicHelper = new AllMusicHelper
  override def findAlbum(d: Document, a: Album): Option[Url] = {
    def score(other: Album): Double = AlbumReconScorer.apply(a, other)
    d.select(".discography table tbody tr").asScala
        .flatMap(e =>
          Try(
            Album(
              title = e.select(".title").asScala.head.text,
              year = e.select(".year").asScala.head.text.toInt,
              artist = a.artist)
          ).toOption.map(e -> _))
        .find(e => score(e._2) >= 0.95)
        .map(_._1)
        .map(_.select("td a").asScala
            .head
            .attr("href")
            .mapIf(_.startsWith("http").isFalse).to("http://www.allmusic.com" + _)
            .mapTo(Url))
  }

  override def apply(e: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    it.downloadDocument(e.link +/ "discography")
        .map(findAlbum(_, a))
        .map(_.map(url => e.copy[Album](link = url)))
        .mFilterOpt(_.link |> allMusicHelper.isValidLink)
}
