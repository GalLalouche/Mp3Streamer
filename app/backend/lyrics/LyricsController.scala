package backend.lyrics

import backend.Url
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, InjectedController}

import scala.concurrent.{ExecutionContext, Future}

class LyricsController @Inject()(ec: ExecutionContext, $: LyricsFormatter)
    extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def push(path: String) = Action.async {request =>
    $.push(path, url = request.body.asText.map(Url).get).map(Ok(_))
  }

  private def ok(f: Future[String]): Action[AnyContent] = Action.async {f.map(Ok(_))}
  def get(path: String) = ok($ get path)
  def setInstrumentalSong(path: String) = ok($.setInstrumentalSong(path))
  def setInstrumentalArtist(path: String) = ok($.setInstrumentalArtist(path))
}
