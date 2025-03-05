package backend.new_albums.filler

import java.time.{Clock, Duration}

import backend.mb.AlbumType
import backend.new_albums.AddedAlbumCount
import backend.new_albums.filler.storage.CachedNewAlbumStorage
import backend.recon.Artist
import backend.storage.{AlwaysFresh, DatedFreshness}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordered.orderingToOrdered

import common.rich.func.BetterFutureInstances.betterFutureInstances
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import scalaz.Scalaz.ToBindOps

import common.concurrency.SimpleTypedActor
import common.rich.RichTime.RichLocalDateTime

@Singleton
private[new_albums] class NewAlbumFiller @Inject() private (
    storage: CachedNewAlbumStorage,
    fetcher: NewAlbumFetcher,
    clock: Clock,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def update(maxAge: Duration, maxCachedAlbums: Int)(a: Artist): Future[AddedAlbumCount] =
    storage
      .forArtist(a)
      .map(_.count(_.albumType == AlbumType.Album) > maxCachedAlbums)
      .ifM(
        ifTrue = ignore(s"Ignoring <$a> because it has too many undownloaded albums"),
        ifFalse = storage
          .freshness(a)
          .getOrElseF {
            scribe.trace(s"Storing new artist <$a>")
            storage.resetToEpoch(a)
          }
          .flatMap {
            case AlwaysFresh => ignore(s"Ignoring <$a> due to configuration")
            case DatedFreshness(dt) =>
              val age = dt.age(clock)
              val info = s"<$a> because it is <${age.toDays}> days old (from <${dt.toLocalDate}>)"
              if (age > maxAge) {
                scribe.debug(s"Fetching new data for $info")
                storage.unremoveAll(a) >> fetcher(a).flatMap(finisher.!(_, Set(a)))
              } else {
                scribe.trace(s"Skipping $info")
                Future.successful(0: AddedAlbumCount)
              }
          }
          .listen(stored => if (stored > 0) scribe.debug(s"Stored <$stored> new albums.")),
      )

  // TODO code duplication with RefreshableRetriever
  private val finisher = SimpleTypedActor.async[(Seq[NewAlbumRecon], Set[Artist]), AddedAlbumCount](
    "CacherFiller finisher",
    Function.tupled(storage.storeNew),
  )
  private def ignore(reason: String) = {
    scribe.trace(reason)
    Future.successful(0: AddedAlbumCount)
  }
}
