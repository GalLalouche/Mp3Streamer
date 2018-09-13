package backend.external

import java.time.{Clock, Duration}

import backend.Retriever
import backend.external.expansions.{AlbumLinkExpanders, ArtistLinkExpanders, CompositeSameHostExpander, ExternalLinkExpander}
import backend.external.extensions._
import backend.external.recons.LinkRetrievers
import backend.recon._
import backend.recon.Reconcilable._
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import backend.storage.{FreshnessStorage, RefreshableStorage}
import common.rich.RichT._
import common.rich.func.{ToMoreFoldableOps, ToMoreMonadErrorOps}
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.{ToBindOps, ToFunctorOps}

private class MbExternalLinksProvider @Inject()(
    ec: ExecutionContext,
    clock: Clock,
    artistReconStorage: ArtistReconStorage,
    artistExternalStorage: ArtistExternalStorage,
    artistLinkRetrievers: LinkRetrievers[Artist],
    albumLinkRetrievers: LinkRetrievers[Album],
    artistLinkExpanders: ArtistLinkExpanders,
    albumLinkExpanders: AlbumLinkExpanders,
    compositeSameHostExpander: CompositeSameHostExpander,
    artistLinkExtractor: ArtistLinkExtractor,
    albumLinkExtractor: AlbumLinkExtractor,
    albumReconStorage: AlbumReconStorage,
    albumExternalStorage: AlbumExternalStorage,
    extender: CompositeExtender,
    mbAlbumReconciler: Reconciler[Album],
    artistReconciler: ReconcilerCacher[Artist],
) extends ToMoreFoldableOps with ToFunctorOps with ToBindOps with ToMoreMonadErrorOps
    with FutureInstances with OptionInstances {
  private implicit val iec: ExecutionContext = ec

  private def wrapExternalPipeWithStorage[R <: Reconcilable : Manifest](
      reconciler: Retriever[R, StoredReconResult],
      storage: ExternalStorage[R],
      provider: Retriever[ReconID, BaseLinks[R]],
      expanders: Traversable[ExternalLinkExpander[R]],
      standaloneReconcilers: LinkRetrievers[R],
  ): Retriever[R, TimestampedLinks[R]] = new RefreshableStorage[R, MarkedLinks[R]](
    new FreshnessStorage(storage, clock),
    new ExternalPipe[R](
      r => reconciler(r).mapEitherMessage({
        case NoRecon => -\/(s"Couldn't reconcile <$r>")
        case HasReconResult(reconId, _) => \/-(reconId)
      }),
      provider,
      standaloneReconcilers,
      expanders,
    ),
    Duration ofDays 28,
    clock,
  ).mapTo(new MbExternalLinksProvider.TimeStamper(_))

  private val artistPipe =
    wrapExternalPipeWithStorage[Artist](
      artistReconciler,
      artistExternalStorage,
      artistLinkExtractor,
      artistLinkExpanders.get,
      artistLinkRetrievers,
    )
  private def getArtistLinks(a: Artist): Future[TimestampedLinks[Artist]] = artistPipe(a)

  private def getAlbumLinks(artistLinks: MarkedLinks[Artist], album: Album): Future[TimestampedLinks[Album]] =
    wrapExternalPipeWithStorage(
      new ReconcilerCacher[Album](albumReconStorage, mbAlbumReconciler),
      albumExternalStorage,
      albumLinkExtractor,
      albumLinkExpanders.get,
      compositeSameHostExpander.toReconcilers(artistLinks.map(_.toBase)) ++ albumLinkRetrievers,
    ) apply album

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
    optionalFuture(recon)(reconId => storage.mapStore(key, {
      case NoRecon => StoredReconResult.unignored(reconId)
      case HasReconResult(_, isIgnored) => HasReconResult(reconId, isIgnored)
    }, default = StoredReconResult.unignored(reconId)))

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
object MbExternalLinksProvider {
  private class TimeStamper[R <: Reconcilable](
      foo: RefreshableStorage[R, MarkedLinks[R]])(implicit ec: ExecutionContext)
      extends Retriever[R, TimestampedLinks[R]] {
    override def apply(r: R): Future[TimestampedLinks[R]] =
      foo.withAge(r).map(e => TimestampedLinks(e._1, e._2.get))
  }
}
