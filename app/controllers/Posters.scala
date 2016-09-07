package controllers

import play.api.mvc.{Action, Controller}

object Posters extends Controller {
	def image(path: String) = Action {
		Ok sendFile Utils.parseFile(path)
	}

	def displayImage(path: String) = Action {
		Ok(views.html.poster("/posters/" + path))
	}
}
