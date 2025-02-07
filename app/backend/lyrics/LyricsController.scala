package backend.lyrics

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import io.lemonlabs.uri.Url
import play.api.mvc.InjectedController

class LyricsController @Inject() (
    $ : LyricsFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  def push(path: String) = converter.parseText(t => $.push(decoder.decode(path), Url.parse(t)))
  def get(path: String) = converter.ok($.get(decoder.decode(path)))
  def setInstrumentalSong(path: String) = converter.ok($.setInstrumentalSong(decoder.decode(path)))
  def setInstrumentalArtist(path: String) =
    converter.ok($.setInstrumentalArtist(decoder.decode(path)))
}
