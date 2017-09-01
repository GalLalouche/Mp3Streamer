package backend.external

import java.time.LocalDateTime

import backend._
import backend.configs.TestConfiguration
import backend.recon.{Album, Artist}
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec}

class AlbumExternalStorageTest extends FreeSpec with AuxSpecs with BeforeAndAfter with BeforeAndAfterAll {
  implicit val c = new TestConfiguration
  val $ = new AlbumExternalStorage()
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
  val album: Album = Album("the spam album", 2000, Artist("foo and the bar band"))

  val link1 = MarkedLink[Album](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), true)
  val link2 = MarkedLink[Album](Url("www.bazqux.com/baz/qux.html"), Host("bazqux", Url("www.bazqux.com")), false)
  "Can load what is stored" in {
    val value = List(link1, link2) -> Some(LocalDateTime.now)
    $.store(album, value).get shouldReturn true
    $.load(album).get.get shouldReturn value
  }
  "No problem with an empty list" in {
    $.store(album, Nil -> None).get shouldReturn true
    $.load(album).get.get._1 shouldReturn Nil
    $.load(album).get.get._2 shouldReturn None
  }
  "Can force store" in {
    $.store(album, Nil -> None).get shouldReturn true
    val link1 = MarkedLink[Album](Url("www.foobar.com/foo/bar.html"), Host("foobar", Url("www.foobar.com")), true)
    $.forceStore(album, List(link1) -> Some(LocalDateTime.now)).get.get shouldReturn (Nil -> None)
  }
  "Delete all links by artist" in {
    val value1: (List[MarkedLink[Album]], None.type) = List(link1) -> None
    $.store(album, value1).get
    val album2 = album.copy(title = "sophomore effort")
    val value2: (List[MarkedLink[Album]], Some[LocalDateTime]) = List(link2) -> Some(LocalDateTime.now)
    $.store(album2, value2).get
    $.deleteAllLinks(album.artist).get.toSet shouldReturn
        Set((album.normalize, value1._1, value1._2), (album2.normalize, value2._1, value2._2))
    $.load(album).get shouldReturn None
    $.load(album2).get shouldReturn None
  }
}
