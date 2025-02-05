package controllers

import java.io.File
import javax.inject.Inject

import play.api.mvc._
import resource.managed

import scala.concurrent.ExecutionContext

class Application @Inject() (ec: ExecutionContext, converter: PlayActionConverter)
    extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def index = Action(Ok.sendFile(new File("public/html/main.html")))

  def sjs() = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/player.html")
    managed(scala.io.Source.fromFile(f.getCanonicalPath)).acquireAndGet(s =>
      Ok(s.mkString).as("text/html"),
    )
  }

  def sjsClient(path: String) = Action {
    val f: File = new File("C:/dev/web/Mp3Streamer/client/" + path)
    managed(scala.io.Source.fromFile(f.getCanonicalPath)).acquireAndGet(s =>
      Ok(s.mkString).as("text/script"),
    )
  }
}
