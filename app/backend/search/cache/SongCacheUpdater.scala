package backend.search.cache

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, ModelJsonable, Song}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import scribe.Level

import scala.concurrent.ExecutionContext

import common.TimedLogger
import common.concurrency.report.ReportObserver
import common.json.saver.JsonableSaver
import common.rich.RichT.richT

private[search] class SongCacheUpdater @Inject() (
    saver: JsonableSaver,
    splitter: SongCacheSaver,
    builder: SongCacheBuilder,
    ec: ExecutionContext,
    mj: ModelJsonable,
    timedLogger: TimedLogger,
) {
  import mj._

  def go(forceRefresh: Boolean): Observable[TimestampedSong] = {
    val original = saver.loadObject[SongCache]
    val $ = ReplaySubject[TimestampedSong]()
    ec.execute(() =>
      builder.updating(original)(new ReportObserver[TimestampedSong, SongCache] {
        override def onStep(a: TimestampedSong) = {
          $.onNext(a)
          scribe.trace(a.toString)
        }
        override def onComplete(result: SongCache): Unit = {
          if (original == result) {
            scribe.info("No change in cache.")
            if (
              saver.exists[ArtistDir] && saver.exists[AlbumDir] && saver.exists[Song] &&
              saver.exists[SongCache]
            )
              if (forceRefresh)
                scribe.info("Force refresh requested, recreating everything")
              else {
                $.onCompleted()
                return
              }
            else
              scribe.info("Some indices are missing, recreating everything.")
          }
          original
            .getDeleted(result)
            .optFilter(_.nonEmpty)
            .foreach(deleted => scribe.info("Deleted files:\n" + deleted.mkString("\n")))

          saver.saveObject(result)
          timedLogger("Recreating indices", Level.Info)(splitter(result.songs))
          $.onCompleted()
        }
        override def onError(t: Throwable) = throw t
      }),
    )
    $
  }
}
