package backend.lyrics

import javax.inject.Inject

import controllers.PlayActionConverter
import io.lemonlabs.uri.Url
import play.api.mvc.InjectedController

class LyricsController @Inject() ($ : LyricsFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def push(path: String) = converter.parseText(t => $.push(path, Url.parse(t)))
  def get(path: String) = converter.ok($.get(path))
  def setInstrumentalSong(path: String) = converter.ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = converter.ok($.setInstrumentalArtist(path))
}
