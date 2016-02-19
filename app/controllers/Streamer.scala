package controllers

import java.io.File
import java.net.URLDecoder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import common.rich.path.Directory
import decoders.DbPowerampCodec
import decoders.FlacDecoder
import play.api.mvc.{ Action, Controller }

object Streamer extends Controller {
  val decoder = new FlacDecoder with DbPowerampCodec {
    val codecPath = "D:/Media/Tools/dBpoweramp/CoreConverter.exe"
    val outputDir = Directory("D:/media/streamer/musicOutput")
  }

  def download(s: String) = Action {
    val futureFile = Future { decoder.decodeFileIfNeeded(new File(URLDecoder.decode(s, "UTF-8"))) }
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

  def playSong(s: String) = Action {
    Ok(views.html.playSong("/music/songs/" + s))
  }
}