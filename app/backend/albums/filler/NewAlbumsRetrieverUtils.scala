package backend.albums.filler

import backend.logging.Logger
import backend.mb.{MbAlbumMetadata, MbArtistReconciler}
import backend.recon.{Album, AlbumReconStorage, Artist, IgnoredReconResult, ReconcilerCacher, ReconID}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import javax.inject.Inject
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.scalaFuture.futureInstance
import common.rich.func.MoreObservableInstances._
import common.rich.func.MoreSeqInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.func.ToMoreMonadPlusOps._
import common.rich.func.ToTraverseMonadPlusOps._

import common.rich.RichObservable._
import common.rich.RichT._

private class NewAlbumsRetrieverUtils @Inject()(
    logger: Logger,
    reconciler: ReconcilerCacher[Artist],
    ec: ExecutionContext,
    meta: MbArtistReconciler,
    albumReconStorage: AlbumReconStorage,
) {
  private implicit val iec: ExecutionContext = ec

  def getReconId(artist: Artist): Observable[ReconID] =
    reconciler(artist).mapEitherMessage {
      case NoRecon => -\/("No recon")
      case HasReconResult(reconId, isIgnored) => if (isIgnored) -\/("Ignored") else \/-(reconId)
    }.foldEither(_.fold(e => {
      logger.debug(s"Did not fetch albums for artist <${artist.name}>; reason: <${e.getMessage}>")
      None
    }, Some.apply)
    ) |> (Observable.from(_).present)

  private def removeIgnoredAlbums(
      artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata) = Album(
      title = album.title, year = album.releaseDate.getYear, artist = artist)
    def isNotIgnored(metadata: MbAlbumMetadata): Future[Boolean] =
      albumReconStorage.isIgnored(toAlbum(metadata))
          .map(_ != IgnoredReconResult.Ignored)
    albums filterTraverse isNotIgnored
  }
  def findNewAlbums(
      cache: ExistingAlbums, artist: Artist, reconId: ReconID): Observable[Seq[NewAlbumRecon]] = {
    val recons: Future[Seq[NewAlbumRecon]] = meta.getAlbumsMetadata(reconId)
        .listen(_ => logger.debug(s"Fetching new albums for <$artist>"))
        .flatMap(removeIgnoredAlbums(artist, _))
        .map(cache.removeExistingAlbums(artist, _))
        .listen(albums => logger.debug(
          s"Finished working on $artist; " +
              s"found ${if (albums.isEmpty) "no" else s"<${albums.size}>"} new albums.")
        )
        .listenError {
          case e: FilteredException => logger.debug(s"<$artist> was filtered, reason: <${e.getMessage}>")
          case e: Throwable => e.printStackTrace()
        }.orElse(Nil)
    Observable.from(recons).filter(_.nonEmpty)
  }
}
