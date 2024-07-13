package controllers

import java.io.File
import javax.imageio.ImageIO
import javax.inject.Inject

import backend.logging.Logger

import scala.concurrent.ExecutionContext

import common.rich.RichT.richT

private class PostersFormatter @Inject() (
    urlPathUtils: UrlPathUtils,
    ec: ExecutionContext,
    logger: Logger,
) {
  def image(path: String): File = urlPathUtils.parseFile(path).<|(validate)

  private def validate(file: File): Unit = ec.execute { () =>
    val image = ImageIO.read(file)
    val height = image.getHeight
    val width = image.getWidth
    lazy val tuple = s"($width X $height)"
    if (height < 500 || width < 500)
      logger.warn(s"Image $file dimensions is too small: $tuple")
    if (height != width)
      logger.warn(s"Image $file isn't square: $tuple")
  }
}
