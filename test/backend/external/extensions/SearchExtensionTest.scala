package backend.external.extensions

import org.scalatest.FreeSpec

import backend.external.{Host, LinkMark}
import backend.recon.Artist
import backend.Url
import common.rich.collections.RichTraversableOnce._
import common.test.AuxSpecs

class SearchExtensionTest extends FreeSpec with AuxSpecs {
  "apply" in {
    val $ = SearchExtension(Host("foo bar", Url("www.foo.bar")), Artist("qux bazz"))
    $.extensions.single.link.address shouldReturn "http://www.google.com/search?q=qux bazz foo bar"
    $.host shouldReturn Host("foo bar", Url("www.foo.bar"))
    $.mark shouldReturn LinkMark.Missing
  }
  "extend missing" in {
    val hosts = Vector(Host.Wikipedia, Host.AllMusic)
    val $ = SearchExtension.extendMissing(hosts, Artist("foo bar"))(
      Vector(ExtendedLink[Artist](Url("???"), Host.Wikipedia, LinkMark.None, Nil)),
    )
    $.map(_.host) shouldMultiSetEqual hosts
    $.find(_.host == Host.AllMusic).get.extensions.single.link.address shouldReturn
      "http://www.google.com/search?q=foo bar AllMusic"
  }
}
