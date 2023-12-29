package backend.search.cache

import javax.inject.Inject

import backend.logging.{FilteringLogger, Logger, LoggingLevel}
import backend.module.StandaloneModule
import com.google.inject.Guice
import models.ModelJsonable
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.concurrent.ExecutionContext

import common.concurrency.report.ReportObserver
import common.io.JsonableSaver
import common.rich.RichFuture.richFuture
import common.rich.RichObservable.richObservable
import common.rich.RichT.richT

private[search] class SongCacheUpdater @Inject() (
    saver: JsonableSaver,
    splitter: SongCacheSplitter,
    builder: SongCacheBuilder,
    logger: Logger,
) {
  import ModelJsonable._

  def go(): Observable[TimestampedSong] = {
    val original = saver.loadObject[SongCache]
    val $ = ReplaySubject[TimestampedSong]()
    builder.updating(original)(new ReportObserver[TimestampedSong, SongCache] {
      override def onStep(a: TimestampedSong) = {
        $.onNext(a)
        println(a)
      }
      override def onComplete(result: SongCache): Unit = {
        $.onCompleted()
        if (original == result) {
          logger.info("No change in cache.")
          return
        }
        original
          .getDeleted(result)
          .optFilter(_.nonEmpty)
          .foreach(deleted => logger.info("Deleted files:\n" + deleted.mkString("\n")))

        saver.saveObject(result)
        import ModelJsonable._
        splitter(result)
      }
      override def onError(t: Throwable) = throw t
    })
    $
  }
}

private object SongCacheUpdater {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(StandaloneModule)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Verbose)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[SongCacheUpdater].go().toFuture[Vector].get
  }
}
