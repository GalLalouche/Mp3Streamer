package controllers

import java.io.File

import javax.inject.Inject

class PostersFormatter @Inject()(urlPathUtils: UrlPathUtils) {
  def image(path: String): File = urlPathUtils.parseFile(path)
}
