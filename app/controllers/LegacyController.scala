package controllers

import com.google.inject.Injector
import play.api.http.{DefaultFileMimeTypesProvider, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.mvc.Controller

class LegacyController extends Controller {
  protected val injector: Injector = ControllerUtils.injector
  protected implicit val mimeTypes: FileMimeTypes =
    new DefaultFileMimeTypesProvider(FileMimeTypesConfiguration(Map(
      "jpg" -> "image/jpeg",
      "png" -> "image/jpeg",
      "jpeg" -> "image/jpeg",
      "mp3" -> "audio/mpeg",
      "flac" -> "audio/x-flac",
      "js" -> "text/javascript",
      "css" -> "text/css",
      "html" -> "text/html"
    ))).get
}
