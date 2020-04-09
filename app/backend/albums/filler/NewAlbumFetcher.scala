package backend.albums.filler

import backend.logging.Logger
import backend.recon.{AlbumReconStorage, Artist, ReconcilerCacher, ReconID}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-, OptionT}
import scalaz.std.scalaFuture.futureInstance
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreMonadErrorOps._

import common.concurrency.DaemonFixedPool
import common.rich.RichT._

@Singleton private class NewAlbumFetcher @Inject()(
    logger: Logger,
    mbDataFetcher: MbDataFetcher,
    albumSaver: AlbumFinisher,
    reconciler: ReconcilerCacher[Artist],
    albumReconStorage: AlbumReconStorage,
) {
  private implicit val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)

  private def getReconId(artist: Artist): OptionT[Future, ReconID] =
    reconciler(artist).mapEitherMessage {
      case NoRecon => -\/("No recon")
      case HasReconResult(reconId, isIgnored) => if (isIgnored) -\/("Ignored") else \/-(reconId)
    }.foldEither(_.fold({e =>
      logger.debug(s"Did not fetch albums for artist <${artist.name}>; reason: <${e.getMessage}>")
      None
    }, Some.apply)
    ) |> OptionT.apply
  def apply(artist: Artist) = {
    (for {
      recon <- getReconId(artist)
      _ = logger.debug(s"Fetching new albums for <$artist>")
      albums <- mbDataFetcher.!(artist, recon).liftSome
    } yield albums) | Nil
  }
}
