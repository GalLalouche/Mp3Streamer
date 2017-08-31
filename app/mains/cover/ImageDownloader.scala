package mains.cover

import java.awt.image.BufferedImage
import java.awt.{Image, RenderingHints}
import javax.swing.ImageIcon

import common.io.RichWSRequest._
import common.io.{DirectoryRef, FileRef, InternetTalker}
import mains.cover.ImageDownloader._

import scala.concurrent.Future

/** Downloads images and saves them to a directory; local image sources will be noop-ed. */
private class ImageDownloader(outputDirectory: DirectoryRef)(implicit it: InternetTalker)
    extends (ImageSource => Future[FolderImage]) {
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

object ImageDownloader {
  def folderImage(f: FileRef, local: Boolean, w: => Int, h: => Int, image: => Image) =
    new FolderImage {
      override def toIcon(requestedWidth: Int, requestedHeight: Int) = {
        val $ = new BufferedImage(requestedWidth, requestedHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = $.createGraphics
        graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.drawImage(image, 0, 0, requestedWidth, requestedHeight, null)
        graphics.dispose()
        new ImageIcon($)
      }
      override val isLocal = local
      override lazy val file = f
      override lazy val width = w
      override lazy val height = h
    }
}
