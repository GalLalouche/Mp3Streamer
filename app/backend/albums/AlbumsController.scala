package backend.albums

import controllers.{PlayActionConverter, UrlDecodeUtils}
import javax.inject.Inject
import play.api.mvc.InjectedController

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
class AlbumsController @Inject()($: AlbumsFormatter, converter: PlayActionConverter, decoder: UrlDecodeUtils)
    extends InjectedController {
  def albums = converter.ok($.albums)
  def forArtist(artistName: String) = converter.ok($.forArtist(decoder.decode(artistName)))

  def removeArtist(artist: String) = converter.noContent($.removeArtist(decoder.decode(artist)))
  def ignoreArtist(artist: String) = converter.noContent($.ignoreArtist(decoder.decode(artist)))
  def unignoreArtist(artist: String) = converter.noContent($.unignoreArtist(decoder.decode(artist)))

  def removeAlbum() = converter.parseJson($.removeAlbum)
  def ignoreAlbum() = converter.parseJson($.ignoreAlbum)

  def index = converter.html("new_albums")
}
