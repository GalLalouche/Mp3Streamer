package mains.cover

import backend.Url
import backend.configs.{Configuration, TestConfiguration}
import backend.external.DocumentSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

class ImageFinderTest extends FreeSpec with DocumentSpecs {
  private implicit val c: Configuration =
    TestConfiguration(_urlToBytesMapper = getBytes("image_search.html").partialConst)
  private val $ = new ImageFinder

  "parse images" in {
    $.find(Url("whatever")).get should contain allOf(
        UrlSource(Url("http://d.ibtimes.co.uk/en/full/1455838/google-foobar.jpg"), 1403, 832),
        UrlSource(Url("https://pbs.twimg.com/profile_images/1135583487/foo_400x400.jpg"), 400, 400),
        UrlSource(Url("https://www.dining-out.co.za/ftp/logo/FooBarCafeGeorgeLogo.gif"), 275, 280),
        UrlSource(Url("https://khromov.files.wordpress.com/2011/02/foobar_cover.png"), 1360, 872))
  }
}
