package backend.search

import backend.recent.{LastAlbumState, NewDir}
import backend.search.cache.SongCacheUpdater
import com.google.inject.{Inject, Singleton}
import rx.lang.scala.Observer
import songs.selector.SongSelectorState

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToBindOps

import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.rich.RichFuture.richFuture
import common.rich.RichObservable.richObservable
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/**
 * Wraps [[SongCacheUpdater]] so it updates [[SearchState], [[SongSearchState]], and
 * [[LastAlbumState]] at the end, as well as update the recent directory observer.
 */
@Singleton private class IndexerUniqifier @Inject() (
    songCacheUpdater: SongCacheUpdater,
    @NewDir newDirObserver: Observer[DirectoryRef],
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    lastAlbumState: LastAlbumState,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  private val uniqueExtra = SimpleActor.unique[Boolean](
    "Indexer extra uniqifier",
    forceRefresh => {
      val $ = Promise[Unit]()
      songCacheUpdater
        .go(forceRefresh)
        .groupByBuffer(_.song.toTuple(_.artistName, _.albumName))
        .doOnNext(newDirObserver onNext _._2.mapSingle(_.song.file.parent))
        .doOnCompleted($.complete(Success(Unit)))
        .doOnCompleted(
          songSelectorState.update() >> searchState.update() >> lastAlbumState.update(),
        )
        .subscribe()
      $.future.get
    },
  )
  def go(forceRefresh: Boolean): Future[Unit] = uniqueExtra ! forceRefresh
}
