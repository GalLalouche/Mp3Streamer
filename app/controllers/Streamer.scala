package controllers

import common.rich.RichT._
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class Streamer @Inject()(
    ec: ExecutionContext,
    $: StreamerFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def download(path: String) = converter.parse(
    _.toTuple(_.headers.get("Range"), PlayControllerUtils.shouldEncodeMp3)
  ) {case (range, shouldEncode) => $(path, range, shouldEncode)}

  // for debugging; plays the song in the browser instead of downloading it
  // "Temporarily" (07/05/22) disabled, because IntelliJ and Play don't want to play nicely.
  // def playSong(s: String) = Action {
  //   //Ok(views.html.playSong("/stream/download/" + s))
  // }
}