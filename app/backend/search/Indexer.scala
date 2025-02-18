package backend.search

import javax.inject.Inject

import backend.recent.{LastAlbumState, NewDir}
import backend.search.cache.SongCacheUpdater
import rx.lang.scala.Observer
import songs.selector.SongSelectorState

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToBindOps

import common.io.DirectoryRef
import common.rich.RichObservable.richObservable
import common.rich.RichObservableSpecVer.richObservableSpecVer
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/**
 * Wraps [[SongCacheUpdater]] so it updates [[SearchState] and [[SongSearchState]] at the end, as
 * well as update the recent directory observer.
 */
private class Indexer @Inject() (
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    songCacheUpdater: SongCacheUpdater,
    lastAlbumState: LastAlbumState,
    @NewDir newDirObserver: Observer[DirectoryRef],
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def index(): Future[_] =
    songCacheUpdater
      .go()
      .groupByBuffer(_.song.toTuple(_.artistName, _.albumName))
      .doOnNext(newDirObserver onNext _._2.mapSingle(_.file.parent))
      .doOnCompleted {
        songSelectorState.update() >> searchState.update() >> lastAlbumState.update()
      }
      .toFuture[Vector]
}
