package controllers

import java.io.File

import net.codingwell.scalaguice.InjectorExtensions._
import play.api.mvc._

import scala.concurrent.ExecutionContext

object Application extends LegacyController {
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  def index = Action {
    Ok(views.html.main())
  }
  def song = Action {
    val file = new File("C:/dev/web/play-2.1.2/Mp3Streamer/public/resources/songs/13 Wonderwall.mp3")
    Status(200).sendFile(file).withHeaders(("Content-length", file.length.toString), ("Accept-Ranges", "bytes"),
      ("X-Pad", "avoid browser bug"), ("Content-Transfer-Encoding", "binary"), ("Cache-Control", "no-cache"),
      ("Content-Disposition", "attachment; filename=" + file.getName.replaceAll(",", "%2c")),
      ("Content-Range", "byte %d/%d".format(file.length, file.length)), ("Content", "audio/mp3"))
  }

  def sjs() = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/player.html")
    Ok(scala.io.Source.fromFile(f.getCanonicalPath).mkString).as("text/html")
  }

  def sjsClient(path: String) = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/" + path)
    Ok(scala.io.Source.fromFile(f.getCanonicalPath).mkString).as("text/script")
  }
}
