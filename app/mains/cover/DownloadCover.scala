package mains.cover

import java.net.URLEncoder

import backend.Url
import backend.configs.{Configuration, StandaloneConfig}
import common.io.{IODirectory, IOFile}
import common.rich.path.{Directory, RichFileUtils, TempDirectory}
import common.rich.path.RichFile.richFile
import models.{AlbumFactory, MusicFinder}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.Process

object DownloadCover {
  private implicit val c: Configuration = StandaloneConfig
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val mf = c.injector.instance[MusicFinder]
  private val albumFactory = c.injector.instance[AlbumFactory]
  import albumFactory._

  private case class CoverException(str: String, e: Exception) extends Exception(e)

  private lazy val tempFolder: Directory = TempDirectory.apply()

  /**
   * Downloads a new image for the album.
   *
   * @param albumDir Should contain the songs. The songs' metadata will be used to search for the correct picture.
   * @return A future command that moves the downloaded file to the input directory and deletes all temporary files.
   */
  def apply(albumDir: Directory): Future[Directory => Unit] = {
    val searchUrl = {
      val album = mf.getSongsInDir(IODirectory(albumDir)).head.album
      val query = URLEncoder.encode(s"${album.artistName} ${album.title}", "UTF-8")
      // tbm=isch => image search; tbs=iar:s => search for square images
      s"https://www.google.com/search?tbm=isch&q=$query&tbs=iar:s"
    }
    for {
      urls <- new ImageFinder find Url(searchUrl)
      locals <- LocalImageFetcher(IODirectory(albumDir))
      selection <- selectImage(locals ++ urls)
    } yield selection match {
      case Selected(img) => fileMover(img)
      case OpenBrowser =>
        // String interpolation is acting funky for some reason (will fail at runtime)
        Process(
          """C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "%s""""
              .format(searchUrl)).!!
        throw new RuntimeException("User opened browser")
      case Cancelled => throw new RuntimeException("User opted out")
    }
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
  }

  private def selectImage(imageURLs: Seq[ImageSource]): Future[ImageChoice] = ImageSelectionPanel(
    ImagesSupplier.withCache(
      imageURLs.iterator.filter(i => i.isSquare && i.width >= 500),
      new ImageDownloader(IODirectory apply tempFolder),
      cacheSize = 12))

  def main(args: Array[String]) {
    implicit val c: Configuration = StandaloneConfig
    import common.rich.RichFuture._
    val folder = Directory(args mkString " ")
    println("Downloading cover image for " + folder.path)
    apply(folder).get.apply(folder)
  }
}
