package backend.external.extensions

import org.scalatest.FreeSpec

import backend.external.{Host, LinkMark}
import backend.recon.Artist
import common.test.AuxSpecs
import io.lemonlabs.uri.Url

class SearchExtensionTest extends FreeSpec with AuxSpecs {
  "apply" in {
    val $ = SearchExtension(Host("foo bar", Url.parse("www.foo.bar")), Artist("qux bazz"))
    val extensions = $.extensions.toVector
    extensions should have length 2
    extensions(0).link shouldReturn Url.parse("https://www.google.com/search?q=qux+bazz+foo+bar")
    extensions(1).link shouldReturn Url.parse("lucky/redirect/qux bazz foo bar")
    $.host shouldReturn Host("foo bar", Url.parse("www.foo.bar"))
    $.mark shouldReturn LinkMark.Missing
  }
  "extend missing" in {
    val hosts = Vector(Host.Wikipedia, Host.AllMusic)
    val $ = SearchExtension.extendMissing(hosts, Artist("foo bar"))(
      Vector(ExtendedLink[Artist](Url.parse("???"), Host.Wikipedia, LinkMark.None, Nil)),
    )
    $.map(_.host) shouldMultiSetEqual hosts
    val extensions = $.find(_.host == Host.AllMusic).get.extensions.toVector
    extensions should have length 2
    extensions(0).link shouldReturn Url.parse("https://www.google.com/search?q=foo+bar+AllMusic")
    extensions(1).link shouldReturn Url.parse("lucky/redirect/foo bar AllMusic")
  }
}
