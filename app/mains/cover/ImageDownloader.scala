package mains.cover

import common.io.{DirectoryRef, FileRef, InternetTalker}
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Downloads images and saves them to a directory. Tries several different unicode encodings. Why an image needs
  * unicode encoding is beyond me.
  *
  * @param outputDirectory The directory to save images to
  * @param downloader      Used to download the images
  */
private class ImageDownloader(outputDirectory: DirectoryRef)(implicit ec: ExecutionContext, it: InternetTalker)
    extends (ImageSource => Future[FolderImage]) {
  private val unicodes = List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16")
  private def toFile(bytes: Array[Byte]): FileRef =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  override def apply(imageSource: ImageSource): Future[FolderImage] = imageSource match {
    case UrlSource(url) => it.asBrowser.bytes(url).map(toFile).map(FolderImage(_, isLocal = false))
    case LocalSource(file) => Future successful FolderImage(file, isLocal = true)
  }
}
