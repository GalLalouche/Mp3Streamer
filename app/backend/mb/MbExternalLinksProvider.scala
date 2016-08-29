package backend.mb

import backend.configs.{CleanConfiguration, Configuration}
import backend.external._
import backend.external.extensions._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage, Retriever}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExtendedExternalLinks] {
  private def createExternalProvider[T <: Reconcilable : Manifest](reconciler: Retriever[T, (Option[ReconID], Boolean)],
                                                                   provider: Retriever[ReconID, Links[T]],
                                                                   expander: Retriever[Links[T], Links[T]]): Retriever[T, Links[T]] =
    new RefreshableStorage(
      new FreshnessStorage(new SlickExternalStorage[T]),
      new ExternalPipe[T](
        a => reconciler(a)
          .filterWith(_._1.isDefined, s"Couldn't reconcile <$a>")
          .map(_._1.get),
        provider,
        expander),
      Duration.standardDays(7))

  private val artistReconciler =
    new ReconcilerCacher[Artist](new ArtistReconStorage, new MbArtistReconciler)
  private val albumReconciler =
    new ReconcilerCacher[Album](new AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private val artistPipe =
    createExternalProvider[Artist](artistReconciler, new ArtistLinkExtractor, Future.successful(Nil).const)
  private val albumPipe = createExternalProvider[Album](albumReconciler, new AlbumLinkExtractor, new AlbumLinksExpander())

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
    implicit val c = CleanConfiguration
    val $ = new MbExternalLinksProvider()
  }
}
