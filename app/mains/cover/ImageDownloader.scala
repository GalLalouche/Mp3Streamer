package mains.cover

import java.awt.Image
import java.util.UUID

import com.google.inject.Inject
import mains.SwingUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Try}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps

import common.concurrency.ExplicitRetriever
import common.io.{DirectoryRef, FileRef, InternetTalkerFactory}
import common.io.RichWSResponse.richWSResponse
import common.rich.RichFuture.richFuture

/** Downloads images and saves them to a directory; local image sources will be noop-ed. */
private class ImageDownloader @Inject() (itf: InternetTalkerFactory) {
  import ImageDownloader._

  def withOutput(
      outputDirectory: DirectoryRef,
  ): ExplicitRetriever[ImageSource, Try[FolderImage]] = ec => {
    implicit val iec: ExecutionContext = ec
    val it = itf.withExecutionContext(ec)

    {
      case UrlSource(url, width, height) =>
        it
          .asBrowser(url, _.bytes, 5.seconds)
          .map { bytes =>
            val file = outputDirectory.addFile(s"${UUID.randomUUID()}-img.jpg").write(bytes)
            folderImage(file, local = false, w = width, h = height, ImageSource.toImage(file))
          }
          .listenError(e => scribe.error(s"Error downloading file <$url>", e))
          .toTry
      case l: LocalSource =>
        Future.successful(Success(folderImage(l.file, local = true, l.width, l.height, l.image)))
    }
  }
}

private object ImageDownloader {
  private def folderImage(f: FileRef, local: Boolean, w: => Int, h: => Int, i: => Image) =
    new FolderImage {
      override def toIcon(requestedWidth: Int, requestedHeight: Int) =
        i.toImageIcon(width = requestedWidth, height = requestedHeight)
      override val isLocal = local
      override lazy val file = f
      override lazy val width = w
      override lazy val height = h
    }
}
