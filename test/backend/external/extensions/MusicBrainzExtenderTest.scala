package backend.external.extensions
import backend.Url
import backend.external.{Host, MarkedLink}
import backend.recon.{Album, Artist}
import common.AuxSpecs
import org.scalatest.FreeSpec

class MusicBrainzExtenderTest extends FreeSpec with AuxSpecs {
  "Preseeded" - {
    "Artist" in {
      val artist = Artist("Foobar")
      val links = Seq(
        MarkedLink[Artist](Url("music.brainz"), Host.MusicBrainz, false),
        MarkedLink[Artist](Url("all.music"), Host.AllMusic, false),
        MarkedLink[Artist](Url("face.book"), Host.Facebook, true),
        MarkedLink[Artist](Url("wiki.pedia"), Host.Wikipedia, false),
        MarkedLink[Artist](Url("last.fm"), Host.LastFm, true),
      )

      val result: Seq[LinkExtension[Artist]] = MusicBrainzArtistExtender.extend(artist, links)

      val preseededEdit = "edit-artist.url.0.text=face.book&edit-artist.url.0.link_type_id=192" +
          "&edit-artist.url.1.text=last.fm&edit-artist.url.1.link_type_id=840"
      result shouldReturn Seq[LinkExtension[Artist]](
        LinkExtension("edit", Url("music.brainz/edit?" + preseededEdit)),
        LinkExtension("Google", Url("http://www.google.com/search?q=foobar MusicBrainz"))
      )
    }
    "Album" in {
      val album = Album("Foo", 2000, Artist("Bar"))
      val links = Seq(
        MarkedLink[Album](Url("music.brainz"), Host.MusicBrainz, false),
        MarkedLink[Album](Url("all.music"), Host.AllMusic, true),
        MarkedLink[Album](Url("wiki.pedia"), Host.Wikipedia, true),
      )

      val result = MusicBrainzAlbumExtender.extend(album, links)

      val preseededEdit = "edit-album.url.0.text=all.music&edit-album.url.0.link_type_id=284" +
          "&edit-album.url.1.text=wiki.pedia&edit-album.url.1.link_type_id=89"
      result shouldReturn Seq[LinkExtension[Album]](
        LinkExtension("edit", Url("music.brainz/edit?" + preseededEdit)),
      )
    }
  }
}
