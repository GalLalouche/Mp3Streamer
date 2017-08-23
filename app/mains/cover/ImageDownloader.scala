package mains.cover

import common.io.{DirectoryRef, FileRef, InternetTalker}

import scala.concurrent.Future

/**
  * Downloads images and saves them to a directory. Tries several different unicode encodings. Why an image needs
  * unicode encoding is beyond me.
  *
  * @param outputDirectory The directory to save images to
  * @param downloader      Used to download the images
  */
private class ImageDownloader(outputDirectory: DirectoryRef)(implicit it: InternetTalker)
    extends (ImageSource => Future[FolderImage]) {
  private def toFile(bytes: Array[Byte]): FileRef =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  override def apply(imageSource: ImageSource): Future[FolderImage] = imageSource match {
    case UrlSource(url) => it.asBrowser.bytes(url).map(toFile).map(FolderImage(_, isLocal = false))
    case LocalSource(file) => Future successful FolderImage(file, isLocal = true)
  }
}
