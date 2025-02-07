package backend.lucky

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayUrlDecoder}
import play.api.mvc.InjectedController

class LuckyController @Inject() (
    $ : LuckyFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  def search(query: String) = converter.ok($.search(PlayUrlDecoder(query)))
  def redirect(query: String) = converter.redirect($.search(PlayUrlDecoder(query)))
}
