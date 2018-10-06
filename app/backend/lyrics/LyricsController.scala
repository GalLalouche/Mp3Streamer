package backend.lyrics

import backend.Url
import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

import scalaz.std.FunctionInstances
import scalaz.syntax.ToContravariantOps

class LyricsController @Inject()(ec: ExecutionContext, $: LyricsFormatter, formatterUtils: FormatterUtils)
    extends InjectedController
        with ToContravariantOps with FunctionInstances {
  private implicit val iec: ExecutionContext = ec

  def push(path: String) = formatterUtils.parseText(t => $.push(path, Url(t)))
  def get(path: String) = formatterUtils.ok($ get path)
  def setInstrumentalSong(path: String) = formatterUtils.ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = formatterUtils.ok($.setInstrumentalArtist(path))
}
