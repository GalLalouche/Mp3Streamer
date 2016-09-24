package backend.external.expansions

import backend.Url
import backend.external.{ExternalLink, Host}
import backend.recon.ReconScorers.AlbumReconScorer
import backend.recon.{Album, Artist, StringReconScorer}
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

private class AllMusicAlbumFinder(implicit ec: ExecutionContext, it: InternetTalker) extends SameHostExpander(Host.AllMusic) {
  val allMusicHelper = new AllMusicHelper
  override def findAlbum(d: Document, a: Album): Option[Url] = {
    val artistName = d.select(".artist-name").head.text
    require(StringReconScorer.apply(artistName, a.artist.name) >= 0.90,
      s"Bad artist name in AllMusicAlbumFinder. Was <$artistName> but was supposed to be <${a.artist.name}.")
    def score(other: Album): Double = AlbumReconScorer.apply(a, other)
    d.select(".discography table tbody tr")
        .toSeq
        .flatMap(e => Try(Album(e.select(".title").head.text, e.select(".year").head.text.toInt, a.artist)).toOption.map(e -> _))
        .find(e => score(e._2) >= 0.95)
        .map(_._1)
        .map(_.select("td a").head.attr("href").mapTo("http://www.allmusic.com" + _).mapTo(Url))
  }

  override def apply(e: ExternalLink[Artist], a: Album): Future[Option[ExternalLink[Album]]] =
    it.downloadDocument(e.link +/ "discography")
        .map(findAlbum(_, a))
        .map(_.map(url => e.copy[Album](link = url)))
        .filterFuture(_.link |> allMusicHelper.isValidLink)
}
