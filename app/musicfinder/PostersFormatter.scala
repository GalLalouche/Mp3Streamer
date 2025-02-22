package musicfinder

import java.io.File
import javax.imageio.ImageIO
import javax.inject.Inject

import genre.GenreFinder
import musicfinder.PostersFormatter.AllowedExtensions

import scala.concurrent.ExecutionContext

import common.io.{FileDownloadValidator, IODirectory}
import common.rich.primitives.RichBoolean.richBoolean

class PostersFormatter @Inject() (
    ec: ExecutionContext,
    genreFinder: GenreFinder,
    fileDownloadValidator: FileDownloadValidator,
) {
  def image(path: String): Option[File] = {
    val file = new File(path)
    fileDownloadValidator(file, AllowedExtensions)
    if (file.exists().isFalse)
      return None
    require(file.isFile, s"File <$file> is a directory")

    validate(file)
    Some(file)
  }

  private def validate(file: File): Unit = ec.execute { () =>
    assert(file.exists)
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
