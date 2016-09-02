package backend.external

import backend.configs.{CleanConfiguration, Configuration}
import backend.external.expansions.{CompositeSameHostExpander, ExternalLinkExpander, LinkExpanders}
import backend.external.extensions._
import backend.external.recons.{Reconciler, Reconcilers}
import backend.mb.{MbAlbumReconciler, MbArtistReconciler}
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage}
import backend.{Retriever, TimestampedLinks}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExtendedExternalLinks] {
  private class Timestamper[R <: Reconcilable](foo: RefreshableStorage[R, Links[R]]) extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] = foo.withAge(r).map(e => TimestampedLinks(e._1, e._2.get))
  }
  private def wrapExternalPipeWithStorage[R <: Reconcilable : Manifest](reconciler: Retriever[R, (Option[ReconID], Boolean)],
                                                                        provider: Retriever[ReconID, Links[R]],
                                                                        expander: Traversable[ExternalLinkExpander[R]],
                                                                        additionalReconciler: Traversable[Reconciler[R]]
                                                                       ): Retriever[R, TimestampedLinks[R]] =
    new RefreshableStorage[R, Links[R]](
      new FreshnessStorage(new SlickExternalStorage),
      new ExternalPipe[R](
        a => reconciler(a)
            .filterWith(_._1.isDefined, s"Couldn't reconcile <$a>")
            .map(_._1.get),
        provider,
        expander,
        additionalReconciler),
      Duration standardDays 7)
        .mapTo(new Timestamper(_))

  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val artistPipe =
    wrapExternalPipeWithStorage[Artist](artistReconciler, new ArtistLinkExtractor, Nil, Reconcilers.artist)
  private def getArtistLinks(a: Artist): Future[TimestampedLinks[Artist]] = artistPipe(a)

  private def getAlbumLinks(artistLinks: Links[Artist], album: Album): Future[TimestampedLinks[Album]] =
    wrapExternalPipeWithStorage(
      new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get))),
      new AlbumLinkExtractor,
      LinkExpanders.albums,
      CompositeSameHostExpander.default.toReconcilers(artistLinks) ++ Reconcilers.album) apply album

  private val extender = CompositeExtender.default

  // for testing on remote
  private def apply(a: Album): Future[ExtendedExternalLinks] =
  for (artistLinks <- getArtistLinks(a.artist);
       albumLinks <- getAlbumLinks(artistLinks.links, a)) yield
    ExtendedExternalLinks(artistLinks.links map extender[Artist], artistLinks.timestamp,
      albumLinks.links map extender[Album], albumLinks.timestamp,
      Nil, null)
  override def apply(s: Song): Future[ExtendedExternalLinks] = apply(s.release)
}

object MbExternalLinksProvider {

  import common.rich.path.Directory
  import common.rich.path.RichFile._

  def fromDir(path: String): Song = Directory(path).files.filter(f => Set("mp3", "flac").contains(f.extension)).head |> Song.apply

  def main(args: Array[String]): Unit = {
    implicit val c = CleanConfiguration
    val $ = new MbExternalLinksProvider()
    //    val f = for (ls <- $.getArtistLinks(Artist("Tori Amos"))) yield
    //      $.getAlbumLinks(ls, Album("Little Earthquakes", 1993, Artist("Tori Amos")))
    //    f.get.get.toList.printPerLine()
  }
}
