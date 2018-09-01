package mains.cover

import backend.Url
import backend.configs.TestConfiguration
import common.{AuxSpecs, MockerWithId}
import common.rich.RichFuture._
import common.rich.primitives.RichBoolean._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.{ExecutionContext, Future}

class ImagesSupplierTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private val mockerWithId = new MockerWithId
  private implicit val ec: ExecutionContext = TestConfiguration().injector.instance[ExecutionContext]
  private def downloadImage(is: ImageSource): Future[FolderImage] =
    Future successful mockerWithId(is match {
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
    new RemainingIterator(Seq("foo", "bar").map(e => UrlSource(Url(e), width = 500, height = 500)): _*)
  "Simple" - {
    val $ = ImagesSupplier(urls, downloadImage)
    "Should fetch when needed" in {
      urls.remaining shouldReturn 2
      $.next().get shouldReturn mockerWithId("foo")
      urls.remaining shouldReturn 1
      $.next().get shouldReturn mockerWithId("bar")
    }
    "Should throw an exception when out of nexts" in {
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
  "Cached" - {
    "Should prefetch" in {
      class TogellableImageDownloader extends (ImageSource => Future[FolderImage]) {
        var stopped = false
        override def apply(is: ImageSource) =
          if (stopped.isFalse) downloadImage(is)
          else Future failed new IllegalStateException("Stopped")
        def stop() = stopped = true
      }
      val downloader = new TogellableImageDownloader
      val $ = ImagesSupplier.withCache(urls, downloader, 2)
      // Should start downloading immediately; since it's on the same thread, it will block until done.
      downloader.stop()
      // If it didn't start, next would fail.
      $.next().value.get.get shouldReturn mockerWithId("foo")
      $.next().value.get.get shouldReturn mockerWithId("bar")
    }
    "Should not fetch more than allotted size" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 1)
      urls.remaining shouldReturn 1
      $.next().value.get.get shouldReturn mockerWithId("foo")
      urls.remaining shouldReturn 0
      $.next().value.get.get shouldReturn mockerWithId("bar")
    }
    "Should not throw if tried to fetch more than available" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 5)
      $.next().value.get.get shouldReturn mockerWithId("foo")
    }
    "But should throw on enough nexts" in {
      val $ = ImagesSupplier.withCache(urls, downloadImage, 5, 10)
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
}
