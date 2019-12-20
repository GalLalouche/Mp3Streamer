package mains.cover.image

import backend.Url
import backend.external.DocumentSpecs
import org.scalatest.OptionValues._
import mains.cover.UrlSource
import org.scalatest.FreeSpec

class OldParserTest extends FreeSpec with DocumentSpecs {
  "parse images on old format" in {
    OldParser(getDocument("old.html")).value should contain allOf(
        UrlSource(Url("http://d.ibtimes.co.uk/en/full/1455838/google-foobar.jpg"), 1403, 832),
        UrlSource(Url("https://pbs.twimg.com/profile_images/1135583487/foo_400x400.jpg"), 400, 400),
        UrlSource(Url("https://www.dining-out.co.za/ftp/logo/FooBarCafeGeorgeLogo.gif"), 275, 280),
        UrlSource(Url("https://khromov.files.wordpress.com/2011/02/foobar_cover.png"), 1360, 872),
    )
  }
  "fail on new format" in {
    OldParser(getDocument("new.html")) shouldReturn None
  }
}
