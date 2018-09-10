package controllers

import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

// TODO Move to its own package?
class Posters @Inject()(implicit ec: ExecutionContext) extends InjectedController {
  def image(path: String) = Action {
    Ok sendFile UrlPathUtils.parseFile(path)
  }
}
