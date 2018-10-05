package controllers

import common.rich.func.ToMoreFoldableOps
import decoders.Mp3Encoder
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

class Streamer @Inject()(
    ec: ExecutionContext,
    encoder: Mp3Encoder,
    downloader: DownloaderHelper,
    urlPathUtils: UrlPathUtils,
) extends InjectedController
    with ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = ec
  def download(s: String) = Action.async {request =>
    val file = urlPathUtils.parseSong(s).file
    val needsEncoding = ControllerUtils.shouldEncodeMp3(request)
    val codec = if (needsEncoding || file.extension == "mp3") "audio/mpeg" else "audio/flac"
    (if (needsEncoding) encoder ! file else Future.successful(file))
        .map(downloader(_, codec, request).withHeaders("Codec" -> codec))
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/stream/download/" + s))
  }
}