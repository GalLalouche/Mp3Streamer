package backend.albums.filler

import java.time.{Clock, Duration}

import backend.albums.filler.storage.CachedNewAlbumStorage
import backend.logging.{FilteringLogger, Logger, LoggingLevel}
import backend.recon.Artist
import backend.storage.{AlwaysFresh, DatedFreshness}
import backend.RichTime.RichLocalDateTime
import backend.albums.filler.CacherFiller.MaxAge
import javax.inject.{Inject, Singleton}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordered.orderingToOrdered

import scalaz.Scalaz.{ToBindOps, ToTraverseOps}
import common.rich.func.BetterFutureInstances.betterFutureInstances
import common.rich.func.MoreTraverseInstances.traversableTraverse
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps

import common.concurrency.SimpleTypedActor
import common.rich.RichFuture.richFuture

@Singleton private class CacherFiller @Inject()(
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
  def update(a: Artist): Future[_] = storage.freshness(a)
      .getOrElseF {
        logger.verbose(s"Storing new artist <$a>")
        storage.reset(a)
      }
      .flatMap {
        case AlwaysFresh =>
          logger.verbose(s"Ignoring <$a>")
          Future.successful(())
        case DatedFreshness(dt) =>
          val age = dt.age(clock)
          val info = s"<$a> because it is <${age.toDays}> days old (from <${dt.toLocalDate}>)"
          if (age > MaxAge) {
            logger.debug(s"Fetching new data for $info")
            storage.unremoveAll(a) >> fetcher(a).flatMap(finisher.!(_, Set(a)))
          } else {
            logger.verbose(s"Skipping $info")
            Future.successful(0)
          }
      }
      .listen(stored => s"Stored <$stored> new albums.")
}

private object CacherFiller {
  private val MaxAge = Duration.ofDays(60)

  def main(args: Array[String]): Unit = {
    val injector = LocalNewAlbumsModule.overridingStandalone(LocalNewAlbumsModule.default)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Verbose)
    val $ = injector.instance[CacherFiller]
    val artists = injector.instance[ExistingAlbums].artists
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    println(artists.traverse($.update).get)
  }
}

