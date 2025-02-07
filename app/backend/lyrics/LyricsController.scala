package backend.lyrics

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayUrlDecoder}
import io.lemonlabs.uri.Url
import play.api.mvc.InjectedController

class LyricsController @Inject() (
    $ : LyricsFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  def push(path: String) = converter.parseText(t => $.push(PlayUrlDecoder(path), Url.parse(t)))
  def get(path: String) = converter.ok($.get(PlayUrlDecoder(path)))
  def setInstrumentalSong(path: String) = converter.ok($.setInstrumentalSong(PlayUrlDecoder(path)))
  def setInstrumentalArtist(path: String) =
    converter.ok($.setInstrumentalArtist(PlayUrlDecoder(path)))
}
