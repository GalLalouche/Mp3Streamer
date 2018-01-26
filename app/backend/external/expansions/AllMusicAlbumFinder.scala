package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import backend.recon.ReconScorers.AlbumReconScorer
import backend.recon.{Album, Artist}
import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, ToMoreMonadOps, ToMoreMonadPlusOps}
import common.rich.primitives.RichBoolean._
import monocle.{Iso, PSetter, Setter}
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scalaz.std.FutureInstances

private class AllMusicAlbumFinder(allMusicHelper: AllMusicHelper)(implicit it: InternetTalker)
    extends SameHostExpander(Host.AllMusic)
        with FutureInstances with ToMoreMonadOps with ToMoreMonadPlusOps with MoreSeqInstances {
  @VisibleForTesting
  private[expansions] def this()(implicit it: InternetTalker) = this(new AllMusicHelper)
  override def findAlbum(d: Document, album: Album): Option[Url] = {
    def score(other: Album): Double = AlbumReconScorer.apply(album, other)
    d.select(".discography table tbody tr").asScala.toSeq
        .tryMap(albumRow => albumRow ->
            Album(
              title = albumRow.select(".title").asScala.head.text,
              year = albumRow.select(".year").asScala.head.text.toInt,
              artist = album.artist)
        ).find(_._2.|>(score) >= 0.95)
        .map(_._1.select("td a").asScala
            .head
            .attr("href")
            .mapIf(_.startsWith("http").isFalse).to("http://www.allmusic.com" + _)
            .mapTo(Url))
  }

  // TODO create monocle utils and move it there
  private def setThenIso[A, B, C](iso: Iso[A, B], setter: Setter[A, C]): PSetter[A, B, C, C] =
    PSetter(setter.modify(_).andThen(iso.get))
  override def apply(artistLink: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] = {
    val setter: PSetter[BaseLink[Artist], BaseLink[Album], Url, Url] =
      setThenIso(BaseLink.iso, BaseLink.link.asSetter)
    it.downloadDocument(artistLink.link +/ "discography")
        .map(findAlbum(_, a).map(setter.set(_)(artistLink)))
        .mFilterOpt(_.link |> allMusicHelper.isValidLink)
  }
}
