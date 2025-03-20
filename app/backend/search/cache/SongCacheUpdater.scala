package backend.search.cache

import com.google.inject.Inject
import models.ModelJsonable
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.concurrent.ExecutionContext

import common.concurrency.report.ReportObserver
import common.io.JsonableSaver
import common.rich.RichT.richT

private[search] class SongCacheUpdater @Inject() (
    saver: JsonableSaver,
    splitter: SongCacheSplitter,
    builder: SongCacheBuilder,
    ec: ExecutionContext,
) {
  import ModelJsonable._

  def go(): Observable[TimestampedSong] = {
    val original = saver.loadObject[SongCache]
    val $ = ReplaySubject[TimestampedSong]()
    ec.execute(() =>
      builder.updating(original)(new ReportObserver[TimestampedSong, SongCache] {
        override def onStep(a: TimestampedSong) = {
          $.onNext(a)
          // TODO replace with logger
          println(a)
        }
        override def onComplete(result: SongCache): Unit = {
          $.onCompleted()
          if (original == result) {
            scribe.info("No change in cache.")
            return
          }
          original
            .getDeleted(result)
            .optFilter(_.nonEmpty)
            .foreach(deleted => scribe.info("Deleted files:\n" + deleted.mkString("\n")))

          saver.saveObject(result)
          import ModelJsonable._
          splitter(result)
        }
        override def onError(t: Throwable) = throw t
      }),
    )
    $
  }
}
