package backend.external.extensions

import backend.Url
import backend.external.Host
import backend.recon.Artist
import common.AuxSpecs
import common.rich.collections.RichTraversableOnce._
import org.scalatest.FreeSpec

class SearchExtensionTest extends FreeSpec with AuxSpecs {
  "apply" in {
    val $ = SearchExtension(Host("foo bar", Url("www.foo.bar")), Artist("qux bazz"))
    $.extensions.single.link.address shouldReturn "http://www.google.com/search?q=qux bazz foo bar"
    $.host shouldReturn Host("foo bar?", Url("www.foo.bar"))
  }
  "extend missing" in {
    val hosts = Seq(Host.Wikipedia, Host.AllMusic)
    val $ = SearchExtension.extendMissing(
      hosts, Artist("foo bar"))(Seq(ExtendedLink[Artist](Url("???"), Host.Wikipedia, isNew = false, Nil)))
    $.map(_.host.canonical).toSet shouldSetEqual hosts.toSet
    $.find(_.host.canonical == Host.AllMusic).get.extensions.single.link.address shouldReturn
        "http://www.google.com/search?q=foo bar AllMusic"
  }
}
