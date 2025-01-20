package controllers

import java.io.File
import javax.imageio.ImageIO
import javax.inject.Inject

import models.GenreFinder

import scala.concurrent.ExecutionContext

import common.io.IODirectory
import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean

private class PostersFormatter @Inject() (
    urlPathUtils: UrlPathUtils,
    ec: ExecutionContext,
    genreFinder: GenreFinder,
) {
  def image(path: String): File = urlPathUtils.parseFile(path).<|(validate)

  private def validate(file: File): Unit = ec.execute { () =>
    val image = ImageIO.read(file)
    val height = image.getHeight
    val width = image.getWidth
    lazy val tuple = s"($width X $height)"
    def warnOnCompositeGenres(s: String): Unit =
      if (genreFinder(IODirectory(file.getParent)).isFlat.isFalse)
        scribe.warn(s)
    if (height < 500 || width < 500)
      warnOnCompositeGenres(s"Image $file dimensions is too small: $tuple")
    if (height != width)
      warnOnCompositeGenres(s"Image $file isn't square: $tuple")
  }
}
