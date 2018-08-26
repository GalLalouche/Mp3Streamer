package backend.external

import java.time.{Clock, Duration}

import backend.Retriever
import backend.configs.Configuration
import backend.external.expansions.{CompositeSameHostExpander, ExternalLinkExpander, LinkExpanders}
import backend.external.extensions._
import backend.external.recons.{AlbumLinkRetrievers, ArtistLinkRetrievers, LinkRetriever, LinkRetrievers}
import backend.mb.{MbAlbumReconciler, MbArtistReconciler}
import backend.recon._
import backend.recon.Reconcilable._
import backend.storage.{FreshnessStorage, RefreshableStorage}
import common.rich.RichT._
import common.rich.func.{ToMoreFoldableOps, ToMoreMonadErrorOps}
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.{ToBindOps, ToFunctorOps}

private class MbExternalLinksProvider(implicit c: Configuration)
    extends ToMoreFoldableOps with ToFunctorOps with ToBindOps with ToMoreMonadErrorOps
        with FutureInstances with OptionInstances {
  private class TimeStamper[R <: Reconcilable](foo: RefreshableStorage[R, MarkedLinks[R]])
      extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] =
      foo.withAge(r).map(e => TimestampedLinks(e._1, e._2.get))
  }
  private val injector = c.injector
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val artistReconStorage: ArtistReconStorage = injector.instance[ArtistReconStorage]
  private val artistExternalStorage = injector.instance[ArtistExternalStorage]
  private val clock = injector.instance[Clock]
  private val artistLinkRetrievers = injector.instance[ArtistLinkRetrievers]
  private val albumLinkRetrievers = injector.instance[AlbumLinkRetrievers]

  private def wrapExternalPipeWithStorage[R <: Reconcilable : Manifest](
      reconciler: Retriever[R, (Option[ReconID], Boolean)],
      storage: ExternalStorage[R],
      provider: Retriever[ReconID, BaseLinks[R]],
      expanders: Traversable[ExternalLinkExpander[R]],
      standaloneReconcilers: LinkRetrievers[R],
  ): Retriever[R, TimestampedLinks[R]] = new RefreshableStorage[R, MarkedLinks[R]](
    new FreshnessStorage(storage, clock),
    new ExternalPipe[R](
      r => reconciler(r)
          .filterWithMessage(_._1.isDefined, s"Couldn't reconcile <$r>")
          .map(_._1.get),
      provider, standaloneReconcilers, expanders),
    Duration ofDays 28,
    clock,
  ).mapTo(new TimeStamper(_))

  private val artistReconciler =
    new ReconcilerCacher[Artist](artistReconStorage, injector.instance[MbArtistReconciler])
  private val artistPipe =
    wrapExternalPipeWithStorage[Artist](
      artistReconciler, artistExternalStorage, new ArtistLinkExtractor, LinkExpanders.artists, artistLinkRetrievers)
  private def getArtistLinks(a: Artist): Future[TimestampedLinks[Artist]] = artistPipe(a)

  private val albumReconStorage: AlbumReconStorage = injector.instance[AlbumReconStorage]
  private val albumExternalStorage = injector.instance[AlbumExternalStorage]
  private def getAlbumLinks(artistLinks: MarkedLinks[Artist], album: Album): Future[TimestampedLinks[Album]] =
    wrapExternalPipeWithStorage(
      new ReconcilerCacher[Album](albumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get))),
      albumExternalStorage,
      new AlbumLinkExtractor,
      LinkExpanders.albums,
      CompositeSameHostExpander.default.toReconcilers(artistLinks.map(_.toBase)) ++ albumLinkRetrievers) apply album

  private val extender = CompositeExtender.default

  // for testing on remote
  private def apply(a: Album): ExtendedExternalLinks = {
    val artistLinks = getArtistLinks(a.artist)
    val albumLinks = artistLinks.flatMap(l => getAlbumLinks(l.links, a))
    ExtendedExternalLinks(artistLinks.map(extender.apply(a.artist, _)), albumLinks.map(extender.apply(a, _)))
  }
  def apply(s: Song): ExtendedExternalLinks = apply(s.release)

  private def optionalFuture[T](o: Option[T])(f: T => Future[_]): Future[_] =
    o.mapHeadOrElse(f, Future successful Unit)
  private def update[R <: Reconcilable](key: R, recon: Option[ReconID], storage: ReconStorage[R]): Future[_] =
    optionalFuture(recon)(reconId => storage.mapStore(key, e => Some(reconId) -> e._2, Some(reconId) -> false))

  def delete(song: Song): Future[_] =
    artistExternalStorage.delete(song.artist) >> albumExternalStorage.delete(song.release)
  def updateRecon(song: Song, artistReconId: Option[ReconID], albumReconId: Option[ReconID]): Future[_] = {
    require(artistReconId.isDefined || albumReconId.isDefined, "No actual recon IDs given")
    update(song.artist, artistReconId, artistReconStorage).>>(
      if (artistReconId.isDefined) {
        artistExternalStorage.delete(song.artist) >>
            albumReconStorage.deleteAllRecons(song.artist) >>
            albumExternalStorage.deleteAllLinks(song.artist)
      } else {
        assert(albumReconId.isDefined)
        albumReconStorage.delete(song.release) >>
            albumExternalStorage.delete(song.release)
      }).>>(update(song.release, albumReconId, albumReconStorage))
  }
}
