package backend.albums

import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

/** A web interface to new albums finder. Displays new albums and can update the current file / ignoring policy. */
class AlbumsController @Inject()($: AlbumsFormatter, formatterUtils: FormatterUtils)
    extends InjectedController {
  def albums = formatterUtils.ok($.albums)

  def removeArtist() = formatterUtils.parseText($.removeArtist)
  def ignoreArtist() = formatterUtils.parseText($.ignoreArtist)

  def removeAlbum() = formatterUtils.parseJson($.removeAlbum)
  def ignoreAlbum() = formatterUtils.parseJson($.ignoreAlbum)

  def index = Action {
    Ok(views.html.new_albums())
  }
}
