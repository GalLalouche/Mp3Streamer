package mains.cover

import common.io.{DirectoryRef, FileRef}
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Downloads images and saves them to a directory. Tries several different unicode encodings. Why an image needs
  * unicode encoding is beyond me.
  *
  * @param outputDirectory The directory to save images to
  * @param downloader      Used to download the images
  */
private class ImageDownloader(outputDirectory: DirectoryRef, downloader: Downloader)(implicit ec: ExecutionContext)
    extends (ImageSource => Future[FolderImage]) {
  private val unicodes = List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16")
  private def toFile(bytes: Array[Byte]): FileRef =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  // TODO T, and T => Future with Task
  // TODO extract
  private def firstSucceededOf[T, S](ts: List[T], f: T => Future[S]): Future[S] = ts match {
    case Nil => Future.failed(new Exception("Ran out of tries"))
    case t :: tail => f(t).orElseTry(firstSucceededOf(tail, f))
  }

  override def apply(imageSource: ImageSource): Future[FolderImage] = imageSource match {
    case UrlSource(url) =>
      firstSucceededOf[String, FolderImage](unicodes, enc => downloader.download(url, enc)
          .map(toFile)
          .map(FolderImage(_, isLocal = false)))
    case LocalSource(file) => Future successful FolderImage(file, isLocal = true)
  }
}
