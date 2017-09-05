package mains.cover

import java.net.URLEncoder
import java.nio.file.Files

import backend.Url
import backend.configs.StandaloneConfig
import common.io.{IODirectory, IOFile}
import common.rich.path.RichFile.richFile
import common.rich.path.{Directory, RichFileUtils}
import models.Song

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process.Process

object DownloadCover {
  private implicit val c = StandaloneConfig
  private case class CoverException(str: String, e: Exception) extends Exception(e)

  private lazy val tempFolder: Directory = Directory.apply(Files.createTempDirectory("images").toFile)
  tempFolder.dir.deleteOnExit()

  /**
   * Downloads a new image for the album
   *
   * @param albumDir Should contain the songs. The songs' metadata will be used to search for the
   *                 correct picture.
   * @return A future command to move the downloaded file to the directory and delete all temporary
   *         files.
   */
  def apply(albumDir: Directory): Future[Directory => Unit] = {
    val urlProvider = new SearchUrlProvider(albumDir)
    val localUrls = LocalImageFetcher(IODirectory(albumDir))
    val imageFinder = new ImageFinder
    val imageUrls = imageFinder find Url(urlProvider.manualSearchUrl)
    for (urls <- imageUrls;
         locals <- localUrls;
         selection <- selectImage(locals ++ urls)) yield selection match {
      case Selected(img) => fileMover(img)
      case OpenBrowser =>
        // String interpolation is acting funky for some reason (will fail at runtime)
        Process(
          """C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "%s""""
              .format(urlProvider.manualSearchUrl)).!!
        throw new RuntimeException("User opened browser")
      case Cancelled => throw new RuntimeException("User opted out")
    }
  }

  private class SearchUrlProvider(albumDir: Directory) {
    private val album =
      Song(albumDir.files.filter(e => e.extension == "mp3" || e.extension == "flac").head).album
    private val query = URLEncoder.encode(s"${album.artistName} ${album.title}", "UTF-8")
    // tbm=isch => image search
    private val prefix = s"https://www.google.com/search?tbm=isch&q=$query"
    // tbs=iar:s => search for square images
    val manualSearchUrl: String = prefix + "&tbs=iar:s"
  }

  private def fileMover(f: FolderImage)(outputDirectory: Directory): Unit = {
    val file = f.file.asInstanceOf[IOFile].file
    if (file.parent == outputDirectory && file.name.toLowerCase == "folder.jpg") {
      RichFileUtils.move(file, "folder.jpg")
      return // This can happen if a local file named folder.jpg is chosen.
    }
    val oldFile = outputDirectory \ "folder.jpg"
    if (oldFile.exists)
      RichFileUtils.move(oldFile, "folder.bak.jpg")
    f.move(outputDirectory)
    tempFolder.deleteAll()
    assert(tempFolder.exists == false)
  }

  private def selectImage(imageURLs: Seq[ImageSource]): Future[ImageChoice] = ImageSelectionPanel(
    ImagesSupplier.withCache(
      imageURLs.iterator.filter(i => i.width == i.height && i.width >= 500 && i.height >= 500),
      new ImageDownloader(IODirectory apply tempFolder),
      cacheSize = 12))

  def main(args: Array[String]) {
    // TODO move to a debugging class
    val path =
      if (args.nonEmpty) args.mkString(" ")
      else """D:\Media\Music\Metal\Progressive Metal\Leprous\2017 Malina"""
    val folder = Directory(path)
    println("Downloading cover image for " + path)
    Await.result(apply(folder), Duration.Inf)(folder)
  }
}
