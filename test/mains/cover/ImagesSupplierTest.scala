package mains.cover

import backend.Url
import com.fasterxml.jackson.databind.ser.std.AsArraySerializerBase
import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.MockerWithId
import common.rich.primitives.RichBoolean._
import common.test.{AsyncAuxSpecs, AuxSpecs}

class ImagesSupplierTest extends AsyncFreeSpec with OneInstancePerTest with AsyncAuxSpecs {
  private val mockerWithId = new MockerWithId
  private def downloadImage(is: ImageSource): Future[FolderImage] =
    Future successful mockerWithId[FolderImage](is match {
      case UrlSource(url, _, _) => url.address
      case LocalSource(file) => file.path
    })
  private class RemainingIterator[T](ts: T*) extends Iterator[T] {
    private val iterator = ts.iterator
    private var iterated = 0
    override def hasNext: Boolean = iterator.hasNext
    override def next(): T = {
      val $ = iterator.next()
      iterated += 1
      $
    }
    def remaining: Int = ts.size - iterated
  }
  private val urls =
    new RemainingIterator(Vector("foo", "bar").map(e => UrlSource(Url(e), width = 500, height = 500)): _*)
  "Simple" - {
    val $ = ImagesSupplier(urls, downloadImage)
    "Should fetch when needed" in {
      urls.remaining shouldReturn 2
      // Not replaced with shouldEventuallyReturn due to bad type inferrence.
      $.next().map(_ shouldReturn mockerWithId("foo"))
          .>|(urls.remaining.shouldReturn(1))
          .>>($.next().map(_ shouldReturn mockerWithId("bar")))
    }
    "Should throw an exception when out of nexts" in {
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
  "Cached" - {
    val sameThreadExecutionContext: ExecutionContext = new ExecutionContext {
      override def execute(runnable: Runnable) = runnable.run()
      override def reportFailure(cause: Throwable) = throw cause
    }
    "Should prefetch" in {
      class TogellableImageDownloader extends (ImageSource => Future[FolderImage]) {
        var stopped = false
        override def apply(is: ImageSource) =
          if (stopped.isFalse) downloadImage(is)
          else Future failed new IllegalStateException("Stopped")
        def stop() = stopped = true
      }
      val downloader = new TogellableImageDownloader
      val $ = ImagesSupplier.withCache(urls, downloader, 2)(sameThreadExecutionContext)
      // Should start downloading immediately; since it's on the same thread, it will block until done.
      downloader.stop()
      // If it didn't start, next would fail.
      $.next().value.get.get shouldReturn mockerWithId("foo")
      $.next().value.get.get shouldReturn mockerWithId("bar")
    }
    "Should not fetch more than allotted size" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 1)(sameThreadExecutionContext)
      urls.remaining shouldReturn 1
      $.next().value.get.get shouldReturn mockerWithId("foo")
      urls.remaining shouldReturn 0
      $.next().value.get.get shouldReturn mockerWithId("bar")
    }
    "Should not throw if tried to fetch more than available" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 5)(sameThreadExecutionContext)
      $.next().value.get.get shouldReturn mockerWithId("foo")
    }
    "But should throw on enough nexts" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 5, 10)(sameThreadExecutionContext)
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
}
