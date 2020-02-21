package mains.cover.image

import backend.Url
import backend.external.DocumentSpecs
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{Assertion, AsyncFreeSpec}

import scala.concurrent.Future

import common.rich.RichT._

class ImageFinderTest extends AsyncFreeSpec with DocumentSpecs {
  private def test(path: String): Future[Assertion] = {
    TestModuleConfiguration(_urlToBytesMapper = getBytes(path).partialConst)
        .injector.instance[ImageFinder].find(Url("whatever")).map(_.get should not be 'empty)
  }
  "parse old format" in {
    test("old.html")
  }
  "parse new format" in {
    test("new.html")
  }
  "performs utf decoding" in {
    test("new_unencoded.html")
  }
}
