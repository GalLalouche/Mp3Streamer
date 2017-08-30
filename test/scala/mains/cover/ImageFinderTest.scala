package mains.cover

import backend.Url
import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.{FreeSpec, ShouldMatchers}

class ImageFinderTest extends FreeSpec with DocumentSpecs with ShouldMatchers {
  private implicit val c =
    TestConfiguration(_urlToBytesMapper = getBytes("image_search.html").partialConst)
  private val $ = new ImageFinder()

  "parse images" in {
    val sources = $.find(Url("whatever")).get.map(_.url.address)
    sources should contain allOf("http://d.ibtimes.co.uk/en/full/1455838/google-foobar.jpg",
        "https://pbs.twimg.com/profile_images/1135583487/foo_400x400.jpg",
        "https://www.dining-out.co.za/ftp/logo/FooBarCafeGeorgeLogo.gif",
        "https://khromov.files.wordpress.com/2011/02/foobar_cover.png"
    )
  }
}
