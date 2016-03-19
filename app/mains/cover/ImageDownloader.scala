package mains.cover

import java.io.File

import common.concurrency.Impatient
import common.rich.collections.RichTraversable._
import common.rich.path.Directory
import common.rich.path.RichFile.poorFile

import scala.actors.threadpool.Executors
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Try

/**
 * Downloads images and saves them to a directory. Tries several different unicode encodings.
 * @param outputDirectory The directory to save images to
 * @param downloader Used to download the images
 */
private class ImageDownloader(outputDirectory: Directory, downloader: Downloader, timeout: Duration = 10 seconds) {
  private val impatiently = new Impatient[Option[FolderImage]](timeout)
  def this(outputDirectory: Directory) = this(outputDirectory, new Downloader)

  private def getBytes(url: String, encoding: String): Option[Array[Byte]] =
    Try(downloader.download(url, encoding)).toOption

  private def toFile(bytes: Array[Byte]): File =
    outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

  def download(url: String): Option[FolderImage] = impatiently {
    List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16")
      .view
      .mapDefined(getBytes(url, _))
      .map(toFile)
      .map(FolderImage)
      .headOption
  }.flatten
}
