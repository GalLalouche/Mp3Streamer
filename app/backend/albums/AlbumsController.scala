package backend.albums

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import models.TypeAliases.ArtistName
import play.api.mvc.InjectedController

/**
 * A web interface to new albums finder. Displays new albums and can update the current file /
 * ignoring policy.
 */
class AlbumsController @Inject() (
    $ : AlbumsFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  def albums = converter.ok($.albums)
  def forArtist(artistName: ArtistName) = converter.ok($.forArtist(decoder.decode(artistName)))

  def removeArtist(artist: ArtistName) = converter.noContent($.removeArtist(decoder.decode(artist)))
  def ignoreArtist(artist: ArtistName) = converter.noContent($.ignoreArtist(decoder.decode(artist)))
  def unignoreArtist(artist: ArtistName) =
    converter.noContent($.unignoreArtist(decoder.decode(artist)))

  def removeAlbum() = converter.parseJson($.removeAlbum)
  def ignoreAlbum() = converter.parseJson($.ignoreAlbum)

  def index = converter.html("new_albums")
}
