package controllers

import java.io.File

import javax.inject.Inject
import play.api.mvc._
import resource.managed

import scala.concurrent.ExecutionContext

import common.rich.primitives.RichString._

class Application @Inject()(ec: ExecutionContext, converter: PlayActionConverter) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def index = converter.html("main")

  def song = Action {
    val file = new File("C:/dev/web/play-2.1.2/Mp3Streamer/public/resources/songs/13 Wonderwall.mp3")
    Status(200).sendFile(file).withHeaders(("Content-length", file.length.toString), ("Accept-Ranges", "bytes"),
      ("X-Pad", "avoid browser bug"), ("Content-Transfer-Encoding", "binary"), ("Cache-Control", "no-cache"),
      ("Content-Disposition", "attachment; filename=" + file.getName.simpleReplace(",", "%2c")),
      ("Content-Range", s"byte ${file.length}/${file.length}"), ("Content", "audio/mp3"))
  }

  def sjs() = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/player.html")
    managed(scala.io.Source.fromFile(f.getCanonicalPath)).acquireAndGet(s =>
      Ok(s.mkString).as("text/html")
    )
  }

  def sjsClient(path: String) = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/" + path)
    managed(scala.io.Source.fromFile(f.getCanonicalPath)).acquireAndGet(s =>
      Ok(s.mkString).as("text/script")
    )
  }
}
