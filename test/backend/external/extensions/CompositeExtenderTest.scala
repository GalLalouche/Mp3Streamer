package backend.external.extensions

import java.time.LocalDateTime

import backend.external._
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist, Reconcilable}
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import common.rich.RichT._
import common.test.AuxSpecs

class CompositeExtenderTest extends FreeSpec with AuxSpecs {
  private val $ = TestModuleConfiguration().injector.instance[CompositeExtender]

  private def toMarked[R <: Reconcilable](e: ExtendedLink[R]) =
    MarkedLink[R](e.link, e.host, e.mark)

  private def verify[R <: Reconcilable](
      source: TimestampedLinks[R],
      actual: TimestampedExtendedLinks[R],
      expected: Map[Host, Seq[LinkExtension[R]]],
  ): Unit = {
    actual.timestamp shouldReturn source.timestamp
    actual.links.map(toMarked) shouldReturn source.links

    val sourceHosts = source.links.map(_.host).toSet
    val extendedHosts = actual.links.filter(_.extensions.nonEmpty).map(_.host).toSet
    val unexpectedExtendedHosts = (sourceHosts &~ extendedHosts).filter(expected.contains)
    unexpectedExtendedHosts shouldBe 'empty

    actual.links
      .filter(_.extensions.nonEmpty)
      .map(e => e.host -> e.extensions) shouldMultiSetEqual expected
  }

  "default adds all links" - {
    "artist" in {
      val artist = Artist("Foobar")
      val links = Host.values
        .map(MarkedLink[Artist](Url.parse("foo.bar"), _, LinkMark.None))
        .|>(TimestampedLinks(_, LocalDateTime.now))
      val result = $.apply(artist, links)

      val expected: Map[Host, Seq[LinkExtension[Artist]]] = Map(
        Host.MusicBrainz -> Vector(
          LinkExtension("edit", Url.parse("foo.bar/edit")),
          LinkExtension("Google", Url.parse("https://www.google.com/search?q=MusicBrainz+foobar")),
          LinkExtension("Lucky", Url.parse("lucky/redirect/MusicBrainz foobar")),
        ),
        Host.AllMusic -> Vector(LinkExtension("discography", Url.parse("foo.bar/discography"))),
        Host.LastFm -> Vector(LinkExtension("similar", Url.parse("foo.bar/+similar"))),
      )

      verify(links, result, expected)
    }
    "album" in {
      val album = Album("Foo", 2000, Artist("Bar"))
      val links = Host.values
        .map(MarkedLink[Album](Url.parse("foo.bar"), _, LinkMark.None))
        .|>(TimestampedLinks(_, LocalDateTime.now))
      val result = $.apply(album, links)

      val expected: Map[Host, Seq[LinkExtension[Album]]] = Map(
        Host.MusicBrainz -> Vector(LinkExtension("edit", Url.parse("foo.bar/edit"))),
        Host.AllMusic -> Vector(LinkExtension("similar", Url.parse("foo.bar/similar"))),
      )

      verify(links, result, expected)
    }
  }
}
