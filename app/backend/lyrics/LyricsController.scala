package backend.lyrics

import backend.Url
import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class LyricsController @Inject()(ec: ExecutionContext, $: LyricsFormatter, formatterUtils: FormatterUtils)
    extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def push(path: String) = formatterUtils.parse(_.body.asText.map(Url).get)($.push(path, _))
  def get(path: String) = formatterUtils.ok($ get path)
  def setInstrumentalSong(path: String) = formatterUtils.ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = formatterUtils.ok($.setInstrumentalArtist(path))
}
