package backend.external.extensions

import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.{Album, Artist}
import io.lemonlabs.uri.Url
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class MusicBrainzExtenderTest extends FreeSpec with AuxSpecs {
  "Preseeded" - {
    "Artist" in {
      val artist = Artist("Foobar")
      val links = Vector(
        MarkedLink[Artist](Url.parse("music.brainz"), Host.MusicBrainz, LinkMark.None),
        MarkedLink[Artist](Url.parse("all.music"), Host.AllMusic, LinkMark.None),
        MarkedLink[Artist](Url.parse("face.book"), Host.Facebook, LinkMark.New),
        MarkedLink[Artist](Url.parse("wiki.pedia"), Host.Wikipedia, LinkMark.None),
        MarkedLink[Artist](Url.parse("last.fm"), Host.LastFm, LinkMark.New),
      )

      val result: Seq[LinkExtension[Artist]] = MusicBrainzArtistExtender.extend(artist, links)

      val preseededEdit = "edit-artist.url.0.text=face.book&edit-artist.url.0.link_type_id=192" +
        "&edit-artist.url.1.text=last.fm&edit-artist.url.1.link_type_id=840"
      result shouldReturn Seq[LinkExtension[Artist]](
        LinkExtension("edit", Url.parse("music.brainz/edit?" + preseededEdit)),
        LinkExtension("Google", Url.parse("https://www.google.com/search?q=MusicBrainz+foobar")),
        LinkExtension("Lucky", Url.parse("lucky/redirect/MusicBrainz foobar")),
      )
    }
    "Album" in {
      val album = Album("Foo", 2000, Artist("Bar"))
      val links = Vector(
        MarkedLink[Album](Url.parse("music.brainz"), Host.MusicBrainz, LinkMark.None),
        MarkedLink[Album](Url.parse("all.music"), Host.AllMusic, LinkMark.New),
        MarkedLink[Album](Url.parse("wiki.pedia"), Host.Wikipedia, LinkMark.New),
      )

      val result = MusicBrainzAlbumExtender.extend(album, links)

      val preseededEdit = "edit-album.url.0.text=all.music&edit-album.url.0.link_type_id=284"
      result shouldReturn Seq[LinkExtension[Album]](
        LinkExtension("edit", Url.parse("music.brainz/edit?" + preseededEdit)),
      )
    }
  }
}
