package mains.cover

import java.nio.file.Files

import backend.configs.StandaloneConfig
import common.Debug
import common.io.IODirectory
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.Song

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.process.Process

// Uses google image search (not API, actual site) to find images, then displays the images for the user to select a
// good picture. The site is used since the API doesn't allow for a size filter.
object DownloadCover extends Debug {
  private implicit val c = StandaloneConfig
  private case class CoverException(str: String, e: Exception) extends Exception(e)

  private lazy val tempFolder: Directory = Directory.apply(Files.createTempDirectory("images").toFile)
  tempFolder.dir.deleteOnExit()
  private val downloader = new Downloader()

  /**
   * Downloads a new image for the album
   * @param albumDir Should contain the songs. The songs' metadata will be used to search for the correct picture.
   * @return A future command to move the downloaded file to the directory, and delete all temporary files
   */
  def apply(albumDir: Directory): Future[Directory => Unit] = getSearchUrlFor(albumDir) |> { searchUrl =>
    downloader.download(searchUrl, "UTF-8")
      .map(new String(_, "UTF-8"))
      .map(extractImageURLs)
      .flatMap(selectImage)
      .map {
        case Selected(img) => fileMover(img)
        case OpenBrowser =>
          Process( """C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe " """" + searchUrl + "\"").!!
          throw new RuntimeException("User opened browser")
        case Cancelled => throw new RuntimeException("User opted out")
      }
  }

  // constructs the search URL using the songs' metadata
  private def getSearchUrlFor(albumDir: Directory) = {
    val query = Song(albumDir.files.filter(e => e.extension == "mp3" || e.extension == "flac").head)
      .mapTo(song => s"${song.artistName} ${song.albumName}")
    s"https://www.google.com/search?tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&q=$query".replaceAll(" ", "%20")
  }

  private def fileMover(f: FolderImage)(outputDirectory: Directory) {
    val oldFile = outputDirectory \ "folder.jpg"
    if (oldFile.exists)
      oldFile renameTo "folder.bak.jpg"
    f.move(outputDirectory)
    tempFolder.deleteAll()
    assert(tempFolder.exists == false)
  }

  private def extractImageURLs(html: String): Seq[String] =
    """"ou":"[^"]+"""".r
      .findAllIn(html) // assumes format "ou":"<url>". fucking closure :\
      .map(_.dropWhile(_ != ':').drop(2).dropRight(1))
      .toVector

  private def selectImage(imageURLs: Seq[String]): Future[ImageChoice] =
    ImageSelectionPanel(ImagesSupplier.withCache(imageURLs.iterator,
      new ImageDownloader(IODirectory.apply(tempFolder), downloader),
      12))

  def main(args: Array[String]) {
    val path = if (args.nonEmpty)
      args(0)
    else
      return
    val folder = Directory(path)
    println("Downloading cover image for " + path)
    Await.result(apply(folder), Duration.Inf)(folder)
  }
}
