package controllers

import play.api.mvc.Request

object PlayControllerUtils {
  def shouldEncodeMp3(request: Request[_]): Boolean =
    request.headers("User-Agent").contains("Chrome") || request.headers
      .get("Referer")
      .exists(_.endsWith("mp3"))
}
