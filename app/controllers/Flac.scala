package controllers

import java.io.File
import java.net.URLDecoder

import common.Debug
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Flac extends Controller with Debug {
  def raw() = Action.async {
    Future {
      //    Ok.sendFile(new File("""D:\Media\Music\Rock\Neo-Prog\Beardfish\2012 The Void\09 - Note.flac"""))
      val file: File = new File( """D:\Media\Music\Rock\Neo-Prog\Transatlantic\2000 SMTPe\03 - Mystery Train.mp3""")
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
  def foobar = Action {
    Ok(views.html.flac())
  }
}
