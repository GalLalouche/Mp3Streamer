package backend.external.extensions
import java.time.LocalDateTime

import backend.Url
import backend.external._
import backend.recon.{Album, Artist, Reconcilable}
import common.AuxSpecs
import common.rich.RichT._
import org.scalatest.FreeSpec

class CompositeExtenderTest extends FreeSpec with AuxSpecs {
  private val $ = CompositeExtender.default

  private def toMarked[R <: Reconcilable](e: ExtendedLink[R]) =
    MarkedLink[R](link = e.link, host = e.host, isNew = e.isNew)

  private def verify[R <: Reconcilable](source: TimestampedLinks[R],
      actual: TimestampedExtendedLinks[R], expected: Map[Host, Seq[LinkExtension[R]]]): Unit = {
    actual.timestamp shouldReturn source.timestamp
    actual.links.map(toMarked) shouldReturn source.links

    val sourceHosts = source.links.map(_.host).toSet
    val extendedHosts = actual.links.filter(_.extensions.nonEmpty).map(_.host).toSet
    val unexpectedExtendedHosts = (sourceHosts &~ extendedHosts).filter(expected.contains)
    unexpectedExtendedHosts shouldBe 'empty

    actual.links.filter(_.extensions.nonEmpty).map(e => e.host -> e.extensions) shouldSetEqual expected
  }

  "default adds all links" - {
    "artist" in {
      val artist = Artist("Foobar")
      val links = Host.hosts.map(MarkedLink[Artist](Url("foo.bar"), _, false))
          .mapTo(TimestampedLinks(_, LocalDateTime.now))
      val result = $.apply(artist, links)

      val expected: Map[Host, Seq[LinkExtension[Artist]]] = Map(
        Host.MusicBrainz -> Seq(LinkExtension("edit", Url("foo.bar/edit")),
          LinkExtension("Google", Url("http://www.google.com/search?q=foobar MusicBrainz"))),
        Host.AllMusic -> Seq(LinkExtension("discography", Url("foo.bar/discography"))),
        Host.LastFm -> Seq(LinkExtension("similar", Url("foo.bar/+similar")))
      )

      verify(links, result, expected)
    }
    "album" in {
      val album = Album("Foo", 2000, Artist("Bar"))
      val links = Host.hosts.map(MarkedLink[Album](Url("foo.bar"), _, false))
          .mapTo(TimestampedLinks(_, LocalDateTime.now))
      val result = $.apply(album, links)

      val expected: Map[Host, Seq[LinkExtension[Album]]] = Map(
        Host.MusicBrainz -> Seq(LinkExtension("edit", Url("foo.bar/edit")),
          LinkExtension("Google", Url("http://www.google.com/search?q=bar - foo MusicBrainz"))),
        Host.AllMusic -> Seq(LinkExtension("similar", Url("foo.bar/similar")))
      )

      verify(links, result, expected)
    }
  }
}
