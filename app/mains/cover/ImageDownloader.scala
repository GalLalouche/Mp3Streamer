package mains.cover

import java.awt.Image

import backend.Retriever
import com.google.inject.Inject
import mains.SwingUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import common.rich.func.kats.ToMoreMonadErrorOps._

import common.io.{DirectoryRef, FileRef, InternetTalker}
import common.io.RichWSResponse.richWSResponse

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
private class ImageDownloader @Inject() (it: InternetTalker, ec: ExecutionContext) {
  import ImageDownloader._

  private implicit val iec: ExecutionContext = ec
  def withOutput(outputDirectory: DirectoryRef): Retriever[ImageSource, FolderImage] = {
    case UrlSource(url, width, height) =>
      it
        .asBrowser(url, _.bytes, 5.seconds)
        .map { bytes =>
          val file =
            outputDirectory.addFile(s"${System.currentTimeMillis()}img.jpg").write(bytes)
          folderImage(file, local = false, w = width, h = height, ImageSource.toImage(file))
        }
        .listenError(e => scribe.error(s"Error downloading file <$url>", e))
    case l: LocalSource =>
      Future.successful(folderImage(l.file, local = true, l.width, l.height, l.image))
  }
}
