package backend.search.cache

import javax.inject.Inject

import backend.logging.ScribeUtils
import backend.module.StandaloneModule
import com.google.inject.Guice
import models.ModelJsonable
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import scribe.Level

import scala.concurrent.ExecutionContext

import common.concurrency.report.ReportObserver
import common.io.JsonableSaver
import common.rich.RichFuture.richFuture
import common.rich.RichObservableSpecVer.richObservableSpecVer
import common.rich.RichT.richT

private[search] class SongCacheUpdater @Inject() (
    saver: JsonableSaver,
    splitter: SongCacheSplitter,
    builder: SongCacheBuilder,
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
    })
    $
  }
}

private object SongCacheUpdater {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(StandaloneModule)
    ScribeUtils.setRootLevel(Level.Trace)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[SongCacheUpdater].go().toFuture[Vector].get
  }
}
