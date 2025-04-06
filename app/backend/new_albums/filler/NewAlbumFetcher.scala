package backend.new_albums.filler

import backend.mb.{MbAlbumMetadata, MbArtistReconciler}
import backend.new_albums.NewAlbum
import backend.recon.{Artist, ReconcilerCacher, ReconID}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.{-\/, \/-, OptionT}

import common.concurrency.DaemonExecutionContext
import common.rich.RichT._

@Singleton private class NewAlbumFetcher @Inject() (
    meta: MbArtistReconciler,
    reconciler: ReconcilerCacher[Artist],
) {
  private implicit val ec: ExecutionContext =
    DaemonExecutionContext(this.simpleName, n = 10, keepAlive = 1.minute)

  private def getReconId(artist: Artist): OptionT[Future, ReconID] = reconciler(artist)
    .mapEitherMessage {
      case NoRecon => -\/("No recon")
      case HasReconResult(reconId, isIgnored) => if (isIgnored) -\/("Ignored") else \/-(reconId)
    }
    .foldEither(
      _.fold(
        { e =>
          scribe
            .debug(s"Did not fetch albums for artist <${artist.name}>; reason: <${e.getMessage}>")
          None
        },
        Some.apply,
      ),
    ) |> OptionT.apply

  def apply(artist: Artist): Future[Seq[NewAlbumRecon]] = (for {
    recon <- getReconId(artist)
    _ = scribe.debug(s"Fetching new albums for <$artist>")
    albums <- meta.getAlbumsMetadata(recon).liftSome
  } yield albums.map(NewAlbumFetcher.toReconned(artist))) | Nil
}

private object NewAlbumFetcher {
  private def toReconned(artist: Artist)(meta: MbAlbumMetadata): NewAlbumRecon =
    NewAlbumRecon(NewAlbum.from(artist, meta), meta.reconId, meta.disambiguation)
}
