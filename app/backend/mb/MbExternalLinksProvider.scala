package backend.mb

import java.io.File

import backend.external._
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage, Retriever}
import backend.{CleanConfiguration, Configuration, StandaloneConfig}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future

class MbExternalLinksProvider(implicit c: Configuration) extends Retriever[Song, ExtendedExternalLinks] {
  private def createExternalProvider[T <: Reconcilable : Manifest](
      reconciler: Retriever[T, (Option[ReconID], Boolean)],
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

  private val extractor = new AlbumLinkExtractor
  private val sameHostExpander = CompositeSameHostExpander.default
  private def superExtractor(artistLinks: Links[Artist], a: Album): Retriever[Links[Album], Links[Album]] = links => {
    import scalaz.Scalaz._
    new AlbumLinksExpander().apply(links).zip(sameHostExpander.apply(artistLinks, a)).map(_.toList.flatten)
  }

  private def getArtistLinks(a: Artist) = artistPipe(a)
  private def getAlbumLinks(artistLinks: Links[Artist], a: Album) =
    createExternalProvider(albumReconciler,
      extractor,
      superExtractor(artistLinks, a)).apply(a)

  private val extender = new CompositeExtender(
    Map[Host, LinkExtender[Artist]](Host.MusicBrainz -> MusicBrainzExtender),
    Map[Host, LinkExtender[Album]](Host.MusicBrainz -> MusicBrainzExtender))

  // for testing on remote
  private def apply(a: Album): Future[ExtendedExternalLinks] =
    for (artistLinks <- getArtistLinks(a.artist); albumLinks <- getAlbumLinks(artistLinks, a))
      yield ExtendedExternalLinks(artistLinks map (extender(_)), albumLinks map (extender(_)), Nil)
  override def apply(s: Song): Future[ExtendedExternalLinks] = apply(s.release)
}

object MbExternalLinksProvider {
  def main(args: Array[String]) {
    implicit val c = CleanConfiguration
    val $ = new MbExternalLinksProvider()
    val x = $(Album("Scarsick", 2007, Artist("Pain of salvation"))).get
//    val x = $(Song(new File("""D:\Media\Music\Metal\Black Metal\Rotting Christ\2007 Theogonia\01 - The Sign of Prime Creation.flac"""))).get
    println(x)
  }
}
