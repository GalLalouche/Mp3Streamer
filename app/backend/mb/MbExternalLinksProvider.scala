package backend.mb

import backend.Retriever
import backend.configs.{CleanConfiguration, Configuration}
import backend.external._
import backend.external.extensions._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExtendedExternalLinks] {
  private def createExternalProvider[R <: Reconcilable : Manifest](reconciler: Retriever[R, (Option[ReconID], Boolean)],
                                                                   provider: Retriever[ReconID, Links[R]],
                                                                   expander: Retriever[Links[R], Links[R]],
                                                                   additionalReconciler: Retriever[R, Links[R]]): Retriever[R, Links[R]] =
    new RefreshableStorage(
      new FreshnessStorage(new SlickExternalStorage),
      new ExternalPipe[R](
        a => reconciler(a)
          .filterWith(_._1.isDefined, s"Couldn't reconcile <$a>")
          .map(_._1.get),
        provider,
        expander,
        additionalReconciler),
      Duration standardDays 7)

  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private val artistPipe =
    createExternalProvider[Artist](artistReconciler, new ArtistLinkExtractor, Future.successful(Nil).const, CompositeReconciler.artist)
  private val albumPipe = createExternalProvider[Album](albumReconciler, new AlbumLinkExtractor, new AlbumLinksExpander(), CompositeReconciler.album)

  private def getArtistLinks(a: Artist) = artistPipe(a)
  private def getAlbumLinks(artistLinks: Links[Artist], a: Album) = albumPipe(a)

  private val extender = CompositeExtender.default

  // for testing on remote
  private def apply(a: Album): Future[ExtendedExternalLinks] =
  for (artistLinks <- getArtistLinks(a.artist); albumLinks <- getAlbumLinks(artistLinks, a))
    yield ExtendedExternalLinks(artistLinks map extender[Artist], albumLinks map extender[Album], Nil)
  override def apply(s: Song): Future[ExtendedExternalLinks] = apply(s.release)
}

object MbExternalLinksProvider {
  import common.rich.path.Directory
  import common.rich.path.RichFile._
  def fromDir(path: String): Song = Directory(path).files.filter(f => Set("mp3", "flac").contains(f.extension)).head |> Song.apply

  def main(args: Array[String]): Unit = {
    import common.rich.func.RichFoldable._

    import scalaz.Scalaz._
    implicit val c = CleanConfiguration
    val $ = new MbExternalLinksProvider()
    $.getArtistLinks(Artist("Soilwork")).map(_ map $.extender.apply[Artist]).get.toList.printPerLine()
  }
}
