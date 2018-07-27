package controllers

import backend.Zipper
import common.io.{IODirectory, IOFile}
import play.api.mvc.Action

object DownloaderController extends LegacyController {
  private val zipper = new Zipper()
  def download(path: String) = Action.async {
    val file = ControllerUtils.parseFile(path)
    require(file.isDirectory)
    zipper.zip(IODirectory(file.getAbsolutePath))
        .map(f => Ok.sendFile(f.asInstanceOf[IOFile].file, inline = false))
  }
}
