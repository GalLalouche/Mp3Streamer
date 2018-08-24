package controllers

import play.api.mvc.Action

// TODO Move to its own package?
object Posters extends LegacyController {
  def image(path: String) = Action {
    Ok sendFile ControllerUtils.parseFile(path)
  }
}
