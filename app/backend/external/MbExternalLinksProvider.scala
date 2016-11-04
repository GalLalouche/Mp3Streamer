package backend.external

import backend.Retriever
import backend.configs.{CleanConfiguration, Configuration}
import backend.external.expansions.{CompositeSameHostExpander, ExternalLinkExpander, LinkExpanders}
import backend.external.extensions._
import backend.external.recons.{Reconciler, Reconcilers}
import backend.mb.{MbAlbumReconciler, MbArtistReconciler}
import backend.recon.Reconcilable._
import backend.recon._
import backend.storage.{FreshnessStorage, RefreshableStorage}
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.joda.time.Duration

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToFunctorOps}

class MbExternalLinksProvider(implicit c: Configuration)
    extends FutureInstances with ToFunctorOps with ToBindOps {
  private class TimeStamper[R <: Reconcilable](foo: RefreshableStorage[R, Links[R]]) extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] = foo.withAge(r).map(e => TimestampedLinks(e._1, e._2.get))
  }
  private def wrapExternalPipeWithStorage[R <: Reconcilable : Manifest](reconciler: Retriever[R, (Option[ReconID], Boolean)],
                                                                        storage: SlickExternalStorage[R],
                                                                        provider: Retriever[ReconID, Links[R]],
                                                                        expander: Traversable[ExternalLinkExpander[R]],
                                                                        additionalReconciler: Traversable[Reconciler[R]]
                                                                       ): Retriever[R, TimestampedLinks[R]] =
    new RefreshableStorage(
      new FreshnessStorage(storage),
      new ExternalPipe[R](
        a => reconciler(a)
            .filterWith(_._1.isDefined, s"Couldn't reconcile <$a>")
            .map(_._1.get),
        provider,
        expander,
        additionalReconciler),
      Duration standardDays 7)
        .mapTo(new TimeStamper(_))

  private val artistReconStorage: ArtistReconStorage = new ArtistReconStorage
  private val artistExternalStorage: SlickExternalStorage[Artist] = new SlickExternalStorage
  private val artistReconciler =
    new ReconcilerCacher[Artist](artistReconStorage, new MbArtistReconciler)
  private val artistPipe =
    wrapExternalPipeWithStorage[Artist](artistReconciler, artistExternalStorage, new ArtistLinkExtractor, Nil, Reconcilers.artist)
  private def getArtistLinks(a: Artist): Future[TimestampedLinks[Artist]] = artistPipe(a)

  private val albumReconStorage: AlbumReconStorage = new AlbumReconStorage
  private val albumExternalStorage: SlickExternalStorage[Album] = new SlickExternalStorage
  private def getAlbumLinks(artistLinks: Links[Artist], album: Album): Future[TimestampedLinks[Album]] =
    wrapExternalPipeWithStorage(
      new ReconcilerCacher[Album](albumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get))),
      albumExternalStorage,
      new AlbumLinkExtractor,
      LinkExpanders.albums,
      CompositeSameHostExpander.default.toReconcilers(artistLinks) ++ Reconcilers.album) apply album

  private val extender = CompositeExtender.default

  // for testing on remote
  private def apply(a: Album): ExtendedExternalLinks = {
    val artistLinks = getArtistLinks(a.artist)
    val albumLinks = artistLinks.flatMap(l => getAlbumLinks(l.links, a))
    ExtendedExternalLinks(artistLinks.map(extender.apply(a.artist, _)), albumLinks.map(extender.apply(a, _)))
  }
  def apply(s: Song): ExtendedExternalLinks = apply(s.release)

  // creates the future if true, otherwise return an empty future
  private def booleanFuture[T](b: Boolean, f: => Future[_]): Future[Unit] =
  optionalFuture(Option(b) filter identity)(e => f)
  // creates the future if the option exists, otherwise return an empty future
  private def optionalFuture[T](o: Option[T])(f: T => Future[_]): Future[Unit] =
  o.map(f(_).>|(())).getOrElse(Future successful Unit)
  private def update[R <: Reconcilable](key: R, recon: Option[ReconID], storage: ReconStorage[R]): Future[Unit] =
    optionalFuture(recon)(reconId => storage.mapStore(key, e => Some(reconId) -> e._2, Some(reconId) -> false))

  def updateRecon(song: Song, artistReconId: Option[ReconID], albumReconId: Option[ReconID]): Future[Unit] = {
    require(artistReconId.isDefined || albumReconId.isDefined, "No actual recon IDs given")
    update(song.artist, artistReconId, artistReconStorage)
        .>>(update(song.release, albumReconId, albumReconStorage))
        .>>(booleanFuture(artistReconId.isDefined && albumReconId.isEmpty, albumReconStorage delete song.release))
        .>>(booleanFuture(artistReconId.isDefined, artistExternalStorage delete song.artist))
        .>>(booleanFuture(artistReconId.isDefined || albumReconId.isDefined, albumExternalStorage delete song.release))
  }
}

object MbExternalLinksProvider {

  import common.rich.path.Directory
  import common.rich.path.RichFile._

  private def fromDir(path: String): Song = Directory(path).files.filter(f => Set("mp3", "flac").contains(f.extension)).head |> Song.apply

  def main(args: Array[String]): Unit = {
    implicit val c = CleanConfiguration
    val $ = new MbExternalLinksProvider()
    //    val f = for (ls <- $.getArtistLinks(Artist("Tori Amos"))) yield
    //      $.getAlbumLinks(ls, Album("Little Earthquakes", 1993, Artist("Tori Amos")))
    //    f.get.get.toList.printPerLine()
  }
}
