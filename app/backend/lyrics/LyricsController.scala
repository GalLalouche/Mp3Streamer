package backend.lyrics

import backend.Url
import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.InjectedController

class LyricsController @Inject() ($ : LyricsFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def push(path: String) = converter.parseText(t => $.push(path, Url(t)))
  def get(path: String) = converter.ok($.get(path))
  def setInstrumentalSong(path: String) = converter.ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = converter.ok($.setInstrumentalArtist(path))
}
