package backend.search.cache

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import scribe.Level

import scala.concurrent.ExecutionContext

import common.{AvroableSaver, TimedLogger}
import common.io.avro.ModelAvroable
import common.rich.RichT.richT
import common.rx.report.ReportObserver

private[search] class SongCacheUpdater @Inject() (
    saver: AvroableSaver,
    splitter: SongCacheSaver,
    builder: SongCacheBuilder,
    ec: ExecutionContext,
    ma: ModelAvroable,
    timedLogger: TimedLogger,
) {
  import ma._

  def go(forceRefresh: Boolean): Observable[TimestampedSong] = {
    val original = SongCache.load(saver)
    val $ = ReplaySubject[TimestampedSong]()
    ec.execute { () =>
      // TODO use the observer below somehow?
      val timerObserver = timedLogger.async("Updating song cache (async)", Level.Info)
      builder.updating(original)(new ReportObserver[TimestampedSong, SongCache] {
        override def onNext(a: TimestampedSong) = {
          $.onNext(a)
          scribe.trace(a.toString)
        }
        override def onCompleted(result: SongCache): Unit = try {
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

          result.save(saver)
          timedLogger("Recreating indices", Level.Info)(splitter(result.songs))
          $.onCompleted()
        } finally
          timerObserver.onCompleted()
        override def onError(t: Throwable) = throw t
      })
    }
    $
  }
}
