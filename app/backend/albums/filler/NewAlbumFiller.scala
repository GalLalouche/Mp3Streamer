package backend.albums.filler

import java.time.{Clock, Duration}

import backend.albums.filler.storage.CachedNewAlbumStorage
import backend.logging.Logger
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
private class NewAlbumFiller @Inject()(
    storage: CachedNewAlbumStorage,
    fetcher: NewAlbumFetcher,
    clock: Clock,
    ec: ExecutionContext,
    logger: Logger,
) {
  private implicit val iec: ExecutionContext = ec
  // TODO code duplication with RefreshableRetriever
  private val finisher = SimpleTypedActor.async[(Seq[NewAlbumRecon], Set[Artist]), Int](
    "CacherFiller finisher", Function.tupled(storage.storeNew))
  def update(maxAge: Duration)(a: Artist): Future[Int] = storage.freshness(a)
      .getOrElseF {
        logger.verbose(s"Storing new artist <$a>")
        storage.reset(a)
      }
      .flatMap {
        case AlwaysFresh =>
          logger.verbose(s"Ignoring <$a>")
          Future.successful(0)
        case DatedFreshness(dt) =>
          val age = dt.age(clock)
          val info = s"<$a> because it is <${age.toDays}> days old (from <${dt.toLocalDate}>)"
          if (age > maxAge) {
            logger.debug(s"Fetching new data for $info")
            storage.unremoveAll(a) >> fetcher(a).flatMap(finisher.!(_, Set(a)))
          } else {
            logger.verbose(s"Skipping $info")
            Future.successful(0)
          }
      }
      .listen(stored => if (stored > 0) logger.debug(s"Stored <$stored> new albums."))
}
