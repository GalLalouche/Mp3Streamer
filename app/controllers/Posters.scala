package controllers

import play.api.mvc.Action

object Posters extends LegacyController {
	def image(path: String) = Action {
		Ok sendFile ControllerUtils.parseFile(path)
	}

	def displayImage(path: String) = Action {
		Ok(views.html.poster("/posters/" + path))
	}
}
