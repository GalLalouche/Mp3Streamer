package backend.search

import javax.inject.Inject

import backend.recent.NewDir
import backend.search.cache.SongCacheUpdater
import rx.lang.scala.Observer
import songs.selector.SongSelectorState

import scala.concurrent.Future

import common.io.DirectoryRef
import common.rich.RichObservable.richObservable
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
    @NewDir newDirObserver: Observer[DirectoryRef],
) {
  def index(): Future[_] =
    songCacheUpdater
      .go()
      .groupByBuffer(_.song.toTuple(_.artistName, _.albumName))
      .doOnNext(newDirObserver onNext _._2.mapSingle(_.file.parent))
      .doOnCompleted {
        songSelectorState.update()
        searchState.update()
      }
      .toFuture[Vector]
}
