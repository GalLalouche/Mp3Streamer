package controllers

import java.io.File
import javax.imageio.ImageIO
import javax.inject.Inject

import controllers.PostersFormatter.AllowedExtensions
import models.GenreFinder

import scala.concurrent.ExecutionContext

import common.io.{BaseDirectory, DirectoryRef, IODirectory}
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichBoolean.richBoolean

private class PostersFormatter @Inject() (
    ec: ExecutionContext,
    genreFinder: GenreFinder,
    @BaseDirectory baseDir: DirectoryRef,
) {
  def image(path: String): Option[File] = {
    require(
      baseDir.isDescendant(path),
      s"Can only download posters from the music directory <${baseDir.path}>, but path was <$path>",
    )
    val file = new File(path)
    if (file.exists().isFalse)
      return None
    require(file.isFile, s"File <$file> is a directory")
    require(
      AllowedExtensions(file.extension),
      s"Can only download image files, but extension was <${file.extension}>",
    )

    validate(file)
    Some(file)
  }

  private def validate(file: File): Unit = ec.execute { () =>
    require(file.exists)
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

private object PostersFormatter {
  private val AllowedExtensions = Set("jpg", "png", "gif", "webp", "bmp")
}
