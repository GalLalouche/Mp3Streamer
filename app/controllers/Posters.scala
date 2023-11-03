package controllers

import javax.inject.Inject
import scala.concurrent.ExecutionContext

import play.api.mvc.InjectedController

class Posters @Inject() (ec: ExecutionContext, $ : PostersFormatter) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def image(path: String) = Action {
    Ok.sendFile($.image(path))
  }
}
