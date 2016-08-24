package controllers

import java.io.File

import play.api.mvc._

object Application extends Controller {
  def index(mute: Boolean) = Action {
    Ok(views.html.main(mute))
  }
  def song = Action {
    val file = new File("C:/dev/web/play-2.1.2/Mp3Streamer/public/resources/songs/13 Wonderwall.mp3")
    Status(200).sendFile(file).withHeaders(("Content-length", file.length.toString), ("Accept-Ranges", "bytes"),
      ("X-Pad", "avoid browser bug"), ("Content-Transfer-Encoding", "binary"), ("Cache-Control", "no-cache"),
      ("Content-Disposition", "attachment; filename=" + file.getName().replaceAll(",", "%2c")),
      ("Content-Range", "byte %d/%d".format(file.length, file.length)), ("Content", "audio/mp3"))
  }
  var i = 0
  def test() = Action {
    i += 1
    Ok("Hi: " + i)
  }

  def sjs() = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/player.html")
    Ok(scala.io.Source.fromFile(f.getCanonicalPath()).mkString).as("text/html")
  }

  def sjsClient(path: String) = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/" + path)
    Ok(scala.io.Source.fromFile(f.getCanonicalPath()).mkString).as("text/script")
  }

}
