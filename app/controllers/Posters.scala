package controllers

import play.api.mvc._
import java.io.File
import java.net.URLDecoder

object Posters extends Controller {
	def image(path: String) = Action {
		Status(200).sendFile(new File(URLDecoder.decode(path, "UTF-8")))
	}
	
	def displayImage(path: String) = Action {
		Ok(views.html.poster("/posters/" + path))
	}
}