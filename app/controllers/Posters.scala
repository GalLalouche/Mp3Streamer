package controllers

import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

// TODO Move to its own package?
class Posters @Inject()(ec: ExecutionContext, urlPathUtils: UrlPathUtils) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def image(path: String) = Action {
    Ok sendFile urlPathUtils.parseFile(path)
  }
}
