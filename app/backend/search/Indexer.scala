package backend.search

import javax.inject.Inject
import scala.concurrent.Future

import backend.recent.{NewDir, RecentController}
import backend.search.cache.SongCacheUpdater
import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.RichObservable.richObservable
import common.rich.RichT._
import rx.lang.scala.Observer
import songs.selector.SongSelectorState

/**
 * Wraps [[SongCacheUpdater]] so it updates [[SearchState] and [[SongSearchState]] at the end, as
 * well as update the recent directories observer.
 */
private class Indexer @Inject() (
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    songCacheUpdater: SongCacheUpdater,
    recentController: RecentController,
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
        recentController.update()
      }
      .toFuture[Vector]
}
