package backend.external

import backend.recon.Artist
import backend._
import backend.configs.TestConfiguration
import common.AuxSpecs
import common.rich.RichFuture._
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}

class SlickExternalStorageTest extends FreeSpec with AuxSpecs with BeforeAndAfter with BeforeAndAfterAll {
  implicit val c = new TestConfiguration
  val $ = new SlickExternalStorage[Artist]
  val utils = $.utils
  override def beforeAll: Unit = {
    utils.createTable().get shouldReturn true
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
  val artist: Artist = Artist("Foo and the bar band")
  
  "Can load what is stored" in {
    val link1 = ExternalLink[Artist](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")))
    val link2 = ExternalLink[Artist](Url("www.bazqux.com/baz/qux.html"), Host("bazqux", Url("www.bazqux.com")))
    val value = List(link1, link2) -> Some(DateTime.now)
    $.store(artist, value).get shouldReturn true
    $.load(artist).get.get shouldReturn value
  }
  "No problem with an empty list" in {
    $.store(artist, Nil -> None).get shouldReturn true
    $.load(artist).get.get._1 shouldReturn Nil
    $.load(artist).get.get._2 shouldReturn None
  }
  "Can force store" in {
    $.store(artist, Nil -> None).get shouldReturn true
    val link1 = ExternalLink[Artist](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")))
    $.forceStore(artist, List(link1) -> Some(DateTime.now)).get.get shouldReturn (Nil -> None)
  }
}
