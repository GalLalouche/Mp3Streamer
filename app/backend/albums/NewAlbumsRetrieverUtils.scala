package backend.albums

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.{Album, AlbumReconStorage, Artist, IgnoredReconResult, ReconcilerCacher, ReconID}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import common.rich.RichT._
import common.rich.func.{MoreObservableInstances, MoreSeqInstances, MoreTraverseInstances, ToMoreFoldableOps, ToMoreFunctorOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import common.rich.RichObservable._
import javax.inject.Inject
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.{FutureInstances, OptionInstances}

private class NewAlbumsRetrieverUtils @Inject()(
    logger: Logger,
    reconciler: ReconcilerCacher[Artist],
    ec: ExecutionContext,
    meta: MbArtistReconciler,
    albumReconStorage: AlbumReconStorage,
) extends ToMoreMonadErrorOps with ToMoreFunctorOps with FutureInstances
    with ToTraverseMonadPlusOps with MoreSeqInstances with MoreTraverseInstances
    with ToMoreFoldableOps with OptionInstances
    with MoreObservableInstances {
  private implicit val iec: ExecutionContext = ec

  def getReconId(artist: Artist): Observable[ReconID] =
    reconciler(artist).mapEitherMessage {
      case NoRecon => -\/("No recon")
      case HasReconResult(reconId, isIgnored) => if (isIgnored) -\/("Ignored") else \/-(reconId)
    }.foldEither(_.fold(e => {
      logger.info(s"Did not fetch albums for artist<${artist.name}>; reason: ${e.getMessage}")
      None
    }, Some.apply)
    ) |> (Observable.from(_).present)

  private def removeIgnoredAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata) = Album(
      title = album.title, year = album.releaseDate.getYear, artist = artist)
    def isNotIgnored(metadata: MbAlbumMetadata): Future[Boolean] =
      albumReconStorage.isIgnored(toAlbum(metadata))
          .map(_ != IgnoredReconResult.Ignored)
    albums filterTraverse isNotIgnored
  }
  def findNewAlbums(
      cache: ArtistLastYearCache, artist: Artist, reconId: ReconID): Observable[NewAlbumRecon] = {
    logger.debug(s"Fetching new albums for <$artist>")
    val recons: Future[Seq[NewAlbumRecon]] = meta.getAlbumsMetadata(reconId)
        .flatMap(removeIgnoredAlbums(artist, _))
        .map(cache.filterNewAlbums(artist, _))
        .listen(albums => {
          logger.debug(s"Finished working on $artist; found ${if (albums.isEmpty) "no" else albums.size} new albums.")
        })
        .listenError {
          case e: FilteredException => logger.debug(s"$artist was filtered, reason: ${e.getMessage}")
          case e: Throwable => e.printStackTrace()
        }.orElse(Nil)
    Observable.from(recons).flattenElements
  }
}
