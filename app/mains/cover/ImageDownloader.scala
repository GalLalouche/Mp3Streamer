package mains.cover

import java.awt.Image

import backend.Retriever
import backend.configs.Configuration
import common.io.{DirectoryRef, FileRef, InternetTalker}
import common.io.RichWSRequest._
import mains.SwingUtils
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.Future

private object ImageDownloader extends SwingUtils {
  def folderImage(f: FileRef, local: Boolean, w: => Int, h: => Int, image: => Image): FolderImage =
    new FolderImage {
      override def toIcon(requestedWidth: Int, requestedHeight: Int) =
        image.toImageIcon(width = requestedWidth, height = requestedHeight)
      override val isLocal = local
      override lazy val file = f
      override lazy val width = w
      override lazy val height = h
    }
}

/** Downloads images and saves them to a directory; local image sources will be noop-ed. */
private class ImageDownloader(outputDirectory: DirectoryRef)(implicit c: Configuration)
    extends Retriever[ImageSource, FolderImage] {
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
  import ImageDownloader._

  private def toFile(bytes: Array[Byte]): FileRef =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  override def apply(imageSource: ImageSource): Future[FolderImage] = imageSource match {
    case UrlSource(url, width, height) => it.asBrowser(url, _.bytes)
        .map(toFile)
        .map(f => folderImage(f, local = false, w = width, h = height, ImageSource toImage f))
    case l: LocalSource =>
      Future successful folderImage(l.file, local = true, l.width, l.height, l.image)
  }
}
