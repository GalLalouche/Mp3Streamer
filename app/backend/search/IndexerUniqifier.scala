package backend.search

import backend.last_albums.LastAlbumsState
import backend.recent.NewDir
import backend.search.cache.SongCacheUpdater
import com.google.inject.{Inject, Singleton}
import rx.lang.scala.Observer
import songs.selector.SongSelectorState

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success

import cats.implicits.toTraverseOps
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.rich.RichFuture.richFutureBlocking
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rx.RichObservable.richObservable

/**
 * Wraps [[SongCacheUpdater]] so it updates [[SearchState], [[SongSearchState]], and
 * [[LastAlbumState]] at the end, as well as update the recent directory observer.
 */
@Singleton private class IndexerUniqifier @Inject() (
    songCacheUpdater: SongCacheUpdater,
    @NewDir newDirObserver: Observer[DirectoryRef],
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    lastAlbumState: LastAlbumsState,
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
        .doOnCompleted(
          Vector(
            songSelectorState.update(),
            searchState.update(),
            lastAlbumState.update(),
          ).sequence >| $.complete(Success(())),
        )
        .subscribe()
      $.future.get
    },
  )
  def go(forceRefresh: Boolean): Future[Unit] = uniqueExtra ! forceRefresh
}
