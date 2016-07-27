package src.backend.external

import backend.{TestConfiguration, _}
import backend.external.{ExternalLink, Host, SlickExternalStorage}
import backend.recon.Artist
import common.AuxSpecs
import common.RichFuture._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}

class SlickExternalStorageTest extends FreeSpec with AuxSpecs with BeforeAndAfter with BeforeAndAfterAll {
  implicit val c = TestConfiguration
  import c._
  val $ = new SlickExternalStorage[Artist]
  val utils = $.utils
  override def beforeAll: Unit = {
    utils.createTable()
  }
  before {
    utils.clearTable().get
  }
  after {
    utils.clearTable().get
  }
  override def afterAll {
    utils.dropTable().get
  }
  "Can load what is stored" in {
    val link1 = ExternalLink[Artist](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")))
    val link2 = ExternalLink[Artist](Url("www.bazqux.com/baz/qux.html"), Host("bazqux", Url("www.bazqux.com")))
    val artist: Artist = Artist("Foo and the bar band")
    val value = List(link1, link2) -> Some(DateTime.now())
    $.store(artist, value).get shouldReturn true
    $.load(artist).get.get shouldReturn value
  }
}
