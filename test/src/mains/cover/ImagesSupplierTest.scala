package mains.cover

import common.MockitoHelper
import common.RichFuture._
import common.rich.RichT._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}

import scala.concurrent.Future

class ImagesSupplierTest extends FreeSpec with OneInstancePerTest with MockitoSugar
  with ShouldMatchers with MockitoHelper {
  private def downloadImage(url: String): Future[FolderImage] = Future successful mockWithId(url)
  private def downloadImageWithDelay(delayInMillis: Int, url: String): Future[FolderImage] = Future({
    Thread sleep delayInMillis
    mockWithId[FolderImage](url)
  })
  private class SmartIterator[T](ts: T*) extends Iterator[T] {
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
  private val urls: SmartIterator[String] = new SmartIterator("foo", "bar")
  "Simple" - {
    val $ = ImagesSupplier(urls, downloadImage)
    "Should do nothing until next is called" in {
      urls.remaining should be === 2
    }
    "Should build from string" in {
      $.next().get should be === mockWithId("foo")
      $.next().get should be === mockWithId("bar")
    }
    "Should throw an exception when out of nexts" in {
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
  "Cached" - {
    "Should prefetch" in {
      val $ = ImagesSupplier.withCache(urls, downloadImageWithDelay(10, _), 1)
      Thread sleep 200
      $.next().value.log().get.log().get.log() should be === mockWithId("foo")
    }
    "Should not throw if tried to fetch more than available" in {
      val $ = ImagesSupplier.withCache(urls, downloadImageWithDelay(10, _), 5)
      Thread sleep 200
      $.next().value.log().get.log().get.log() should be === mockWithId("foo")
    }
    "But should throw on enough nexts" in {
      val $ = ImagesSupplier.withCache(urls, downloadImageWithDelay(10, _), 5, 10)
      Thread sleep 200
      $.next()
      $.next()
      a[NoSuchElementException] should be thrownBy $.next()
    }
  }
}
