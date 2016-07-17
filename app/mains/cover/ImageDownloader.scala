package mains.cover

import java.io.File

import common.RichFuture._
import common.rich.path.Directory
import common.rich.path.RichFile.poorFile

import scala.concurrent.{ExecutionContext, Future}

/**
 * Downloads images and saves them to a directory. Tries several different unicode encodings.
 * @param outputDirectory The directory to save images to
 * @param downloader      Used to download the images
 */
private class ImageDownloader(outputDirectory: IODirectory, downloader: Downloader)(implicit ec: ExecutionContext) {
  private def toFile(bytes: Array[Byte]): File =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  private def firstSucceededOf[T, S](ts: List[T], f: T => Future[S]): Future[S] = ts match {
    case Nil => Future.failed(new Exception("Ran out of tries"))
    case t :: tail => f(t).orElseTry(firstSucceededOf(tail, f))
  }
  def download(url: String): Future[FolderImage] = firstSucceededOf[String, FolderImage](
    List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16"),
    enc => downloader.download(url, enc)
        .map(toFile)
        .map(FolderImage.apply))
}
