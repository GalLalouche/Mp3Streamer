package controllers

import net.codingwell.scalaguice.InjectorExtensions._
import play.api.mvc.Action

import scala.concurrent.ExecutionContext

// TODO Move to its own package?
object Posters extends LegacyController {
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  def image(path: String) = Action {
    Ok sendFile ControllerUtils.parseFile(path)
  }
}
