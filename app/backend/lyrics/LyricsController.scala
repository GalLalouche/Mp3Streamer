package backend.lyrics

import backend.Url
import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

class LyricsController @Inject()($: LyricsFormatter, formatterUtils: FormatterUtils)
    extends InjectedController {
  def push(path: String) = formatterUtils.parseText(t => $.push(path, Url(t)))
  def get(path: String) = formatterUtils.ok($ get path)
  def setInstrumentalSong(path: String) = formatterUtils.ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = formatterUtils.ok($.setInstrumentalArtist(path))
}
