package controllers

import java.io.File
import java.net.URLDecoder

import decoders.DbPowerampCodec
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Streamer extends Controller {
  val decoder = DbPowerampCodec

  def download(s: String) = Action {
    val futureFile = Future { decoder.encodeFileIfNeeded(new File(URLDecoder.decode(s, "UTF-8"))) }
    Async {
      futureFile.map { file =>
        loggers.CompositeLogger.trace("Sending file " + file.getAbsolutePath)
        Status(200).sendFile(file).withHeaders(
          ("Content-length", file.length.toString),
          ("Accept-Ranges", "bytes"),
          ("X-Pad", "avoid browser bug"),
          ("Content-Transfer-Encoding", "binary"),
          ("Cache-Control", "no-cache"),
          ("Content-Disposition", "attachment; filename=" + file.getName().replaceAll(",", "%2c")),
          ("Content-Range", "byte %d/%d".format(file.length, file.length)),
          ("Content", "audio/mp3")
        )

      }
    }
  }

  // for debugging; plays the song in the browser instead of downloading it
  def playSong(s: String) = Action {
    Ok(views.html.playSong("/data/songs/" + s))
  }
}
