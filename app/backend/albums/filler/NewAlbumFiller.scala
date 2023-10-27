package backend.albums.filler

import java.time.{Clock, Duration}

import backend.albums.filler.storage.CachedNewAlbumStorage
import backend.albums.AddedAlbumCount
import backend.logging.Logger
import backend.mb.AlbumType
import backend.recon.Artist
import backend.storage.{AlwaysFresh, DatedFreshness}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordered.orderingToOrdered

import scalaz.Scalaz.ToBindOps
import common.rich.func.BetterFutureInstances.betterFutureInstances
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps

import common.concurrency.SimpleTypedActor
import common.rich.RichTime.RichLocalDateTime

@Singleton
private[albums] class NewAlbumFiller @Inject() private(
    storage: CachedNewAlbumStorage,
    fetcher: NewAlbumFetcher,
    ea: EagerExistingAlbums,
    clock: Clock,
    ec: ExecutionContext,
    logger: Logger,
) {
  private implicit val iec: ExecutionContext = ec
  // TODO code duplication with RefreshableRetriever
  private val finisher = SimpleTypedActor.async[(Seq[NewAlbumRecon], Set[Artist]), AddedAlbumCount](
    "CacherFiller finisher", Function.tupled(storage.storeNew))
  private def ignore(reason: String) = {
    logger.verbose(reason)
    Future.successful(0: AddedAlbumCount)
  }
  def update(maxAge: Duration, maxCachedAlbums: Int)(a: Artist): Future[AddedAlbumCount] = {
    storage.forArtist(a)
        .map(_.count(_.albumType == AlbumType.Album) > maxCachedAlbums)
        .ifM(
          ifTrue = ignore(s"Ignoring <$a> because it has too many undownloaded albums"),
          ifFalse =
            storage.freshness(a)
                .getOrElseF {
                  logger.verbose(s"Storing new artist <$a>")
                  storage.reset(a)
                }
                .flatMap {
                  case AlwaysFresh => ignore(s"Ignoring <$a> due to configuration")
                  case DatedFreshness(dt) =>
                    val age = dt.age(clock)
                    val info = s"<$a> because it is <${age.toDays}> days old (from <${dt.toLocalDate}>)"
                    if (age > maxAge) {
                      logger.debug(s"Fetching new data for $info")
                      storage.unremoveAll(a) >> fetcher(a).flatMap(finisher.!(_, Set(a)))
                    } else {
                      logger.verbose(s"Skipping $info")
                      Future.successful(0: AddedAlbumCount)
                    }
                }
                .listen(stored => if (stored > 0) logger.debug(s"Stored <$stored> new albums.")),
        )
  }
}
