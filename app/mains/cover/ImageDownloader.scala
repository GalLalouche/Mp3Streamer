package mains.cover

import java.awt.Image
import javax.inject.Inject

import backend.Retriever
import backend.logging.Logger
import mains.SwingUtils._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._

import common.io.{DirectoryRef, FileRef, InternetTalker}
import common.io.RichWSRequest._

private object ImageDownloader {
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
private class ImageDownloader @Inject() (it: InternetTalker, logger: Logger) {
  import ImageDownloader._

  private implicit val iec: ExecutionContext = it
  def withOutput(outputDirectory: DirectoryRef): Retriever[ImageSource, FolderImage] = {
    case UrlSource(url, width, height) =>
      it
        .asBrowser(url, _.bytes, timeoutInSeconds = 5)
        .map { bytes =>
          val file = outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)
          folderImage(file, local = false, w = width, h = height, ImageSource.toImage(file))
        }
        .listenError(e => logger.error(s"Error downloading file <$url>", e))
    case l: LocalSource =>
      Future.successful(folderImage(l.file, local = true, l.width, l.height, l.image))
  }
}
