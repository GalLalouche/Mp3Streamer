package backend.albums

import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.InjectedController

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
class AlbumsController @Inject()($: AlbumsFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def albums = converter.ok($.albums)

  def removeArtist() = converter.parseText($.removeArtist)
  def ignoreArtist() = converter.parseText($.ignoreArtist)

  def removeAlbum() = converter.parseJson($.removeAlbum)
  def ignoreAlbum() = converter.parseJson($.ignoreAlbum)

  def index = Action {
    Ok(views.html.new_albums())
  }
}
