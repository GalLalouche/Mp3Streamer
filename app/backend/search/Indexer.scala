package backend.search

import backend.recent.NewDir
import backend.search.cache.SongCacheUpdater
import javax.inject.Inject
import rx.lang.scala.Observer
import songs.SongSelectorState

import scala.concurrent.{ExecutionContext, Future}

import common.io.DirectoryRef
import common.rich.RichObservable.richObservable
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.RichT._

/**
* Wraps [[SongCacheUpdater]] so it updates [[SearchState] and [[SongSearchState]] at the end, as well as
* update the recent directories observer.
*/
private class Indexer @Inject()(
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    songCacheUpdater: SongCacheUpdater,
    @NewDir newDirObserver: Observer[DirectoryRef],
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def index(): Future[_] =
    songCacheUpdater.go()
        .groupByBuffer(_.song.toTuple(_.artistName, _.albumName))
        .doOnNext(newDirObserver onNext _._2.mapSingle(_.file.parent))
        .doOnCompleted {
          songSelectorState.update()
          searchState.update()
        }
        .toFuture[Vector]
}
