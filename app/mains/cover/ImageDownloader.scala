package mains.cover

import backend.Url
import common.io.{DirectoryRef, FileRef}
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Downloads images and saves them to a directory. Tries several different unicode encodings. Why an image needs
 * unicode encoding is beyond me.
 * @param outputDirectory The directory to save images to
 * @param downloader Used to download the images
 */
private class ImageDownloader(outputDirectory: DirectoryRef, downloader: Downloader)(implicit ec: ExecutionContext)
  extends (Url => Future[FolderImage]) {
  private def toFile(bytes: Array[Byte]): FileRef =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  private def firstSucceededOf[T, S](ts: List[T], f: T => Future[S]): Future[S] = ts match {
    case Nil => Future.failed(new Exception("Ran out of tries"))
    case t :: tail => f(t).orElseTry(firstSucceededOf(tail, f))
  }
  override def apply(url: Url): Future[FolderImage] = firstSucceededOf[String, FolderImage](
    List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16"),
    enc => downloader.download(url, enc)
      .map(toFile)
      .map(FolderImage.apply))
}
