package backend.search

import backend.recent.NewDir
import backend.search.cache.{CacheUpdate, MetadataCacher}
import javax.inject.Inject
import rx.lang.scala.{Observable, Observer}
import songs.SongSelectorState

import common.io.DirectoryRef

/**
* Wraps [[MetadataCacher]] so it updates [[SearchState] and [[SongSearchState]] at the end, as well as update
* the recent directories observer on quick refreshes.
*/
private class Indexer @Inject()(
    searchState: SearchState,
    songSelectorState: SongSelectorState,
    metadataCacher: MetadataCacher,
    @NewDir newDirObserver: Observer[DirectoryRef],
) {
  def cacheAll(): Observable[CacheUpdate] = updateOnCompletion(metadataCacher.cacheAll())
  def quickRefresh(): Observable[CacheUpdate] =
    updateOnCompletion(metadataCacher.quickRefresh().doOnNext(newDirObserver onNext _.dir))

  private def updateOnCompletion(o: Observable[CacheUpdate]): Observable[CacheUpdate] = o.doOnCompleted {
    songSelectorState.update()
    searchState.update()
  }
}
