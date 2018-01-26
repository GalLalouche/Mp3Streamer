package mains.cover

import java.net.URLEncoder
import java.nio.file.Files

import backend.Url
import backend.configs.StandaloneConfig
import common.io.{IODirectory, IOFile}
import common.rich.path.RichFile.richFile
import common.rich.path.{Directory, RichFileUtils}
import common.rich.primitives.RichBoolean._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process.Process

object DownloadCover {
  private implicit val c = StandaloneConfig
  import c._
  private case class CoverException(str: String, e: Exception) extends Exception(e)

  private lazy val tempFolder: Directory = {
    // TODO add createTempDir() to Directory
    val dir = Files.createTempDirectory("images").toFile
    dir.deleteOnExit()
    Directory(dir)
  }

  /**
   * Downloads a new image for the album.
   *
   * @param albumDir Should contain the songs. The songs' metadata will be used to search for the correct picture.
   * @return A future command that moves the downloaded file to the input directory and deletes all temporary files.
   */
  def apply(albumDir: Directory): Future[Directory => Unit] =
    for (urls <- new ImageFinder find Url(new SearchUrlProvider(albumDir).searchUrl);
         locals <- LocalImageFetcher(IODirectory(albumDir));
         selection <- selectImage(locals ++ urls)) yield selection match {
      case Selected(img) => fileMover(img)
      case OpenBrowser =>
        // String interpolation is acting funky for some reason (will fail at runtime)
        Process(
          """C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "%s""""
              .format(new SearchUrlProvider(albumDir).searchUrl)).!!
        throw new RuntimeException("User opened browser")
      case Cancelled => throw new RuntimeException("User opted out")
    }

  private class SearchUrlProvider(albumDir: Directory) {
    private val album = c.mf.getSongsInDir(IODirectory(albumDir)).head.album
    private val query = URLEncoder.encode(s"${album.artistName} ${album.title}", "UTF-8")
    // tbm=isch => image search
    private val prefix = s"https://www.google.com/search?tbm=isch&q=$query"
    // tbs=iar:s => search for square images
    val searchUrl: String = prefix + "&tbs=iar:s"
  }

  private def fileMover(f: FolderImage)(outputDirectory: Directory): Unit = {
    val file = f.file.asInstanceOf[IOFile].file
    if (file.parent == outputDirectory && file.name.toLowerCase == "folder.jpg") {
      RichFileUtils.rename(file, "folder.jpg") // canonize casing
      return // This can happen if a local file named folder.jpg is chosen.
    }

    // backup old folder.jpg file if it exists
    val oldFile = outputDirectory \ "folder.jpg"
    if (oldFile.exists)
      RichFileUtils.rename(oldFile, "folder.bak.jpg")

    f.move(outputDirectory)
    tempFolder.deleteAll()
    assert(tempFolder.exists.isFalse)
  }

  private def selectImage(imageURLs: Seq[ImageSource]): Future[ImageChoice] = ImageSelectionPanel(
    ImagesSupplier.withCache(
      imageURLs.iterator.filter(i => i.width == i.height && i.width >= 500),
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
