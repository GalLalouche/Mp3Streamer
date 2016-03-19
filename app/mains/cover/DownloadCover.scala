package mains.cover

import java.nio.file.Files
import java.util.concurrent.LinkedBlockingQueue

import common.Debug
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichString.richString
import models.Song
import org.jsoup.Jsoup
import resource.managed

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.sys.process.Process

// Uses google image search (not API, actual site) to find images
// Then displays the images for the user to select a good picture
// The site is used since the API doesn't allow for a size filter
object DownloadCover extends Debug {
  case class CoverException(str: String, e: Exception) extends Exception(e)
  lazy val tempFolder: Directory = Directory.apply(Files.createTempDirectory("images").toFile)
  tempFolder.dir.deleteOnExit()

  /**
   * Downloads a new image for the album
   * @param albumDir Should contain the songs. The metadata will be used to search for the correct picture.
   * @return A future command to move the downloaded file to the directory, and delete all temporary files
   */
  def apply(albumDir: Directory): Future[Directory => Unit] = {
    val searchUrl = albumDir |> getSearchUrl
    Future.apply(getHtml(searchUrl))
      .map(extractImageURLs)
      .map(selectImage)
      .map(
        {
          case Selected(img) => fileMover(img)
          case OpenBrowser =>
            openBrowser(searchUrl)
            throw new RuntimeException("User opened browser")
          case Cancelled => throw new RuntimeException("User opted out")
        }
      )
  }

  private def fileMover(f: FolderImage)(dir: Directory) {
    val oldFile = dir \ "folder.jpg"
    if (oldFile.exists)
      oldFile.renameTo("folder.bak.jpg")
    f.move(dir)
    tempFolder.deleteAll()
    assert(tempFolder.exists == false)
  }

  private def getSearchUrl(albumDir: Directory) = {
    val query = Song(albumDir.files.head).mapTo(song => s"${song.artistName } ${song.albumName }")
    s"https://www.google.com/search?tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&q=$query".replaceAll(" ", "%20")
  }
  def openBrowser(searchUrl: String) {
    Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe " """" + searchUrl + "\"").!!
  }

  private def getHtml(url: String): String = {
    new String(new Downloader().download(url, "UTF-8"), "UTF-8")
  }

  private def extractImageURLs(html: String): Seq[String] = Jsoup.parse(html)
    .select("a")
    .map(_.attr("href"))
    .filterNot(_.isEmpty)
    .filter(_.matches(".*imgurl=.*"))
    .map(_.captureWith(".*imgurl=(.*?)\\&.*".r))

  private def selectImage(imageURLs: Seq[String]): ImageChoice =
    managed(new DelayedThread("Image downloader")).acquireAndGet { worker =>
      ImageSelectionPanel.apply(
        n => {
          val queue = new LinkedBlockingQueue[FolderImage](n * 2)
          val queuer = new ImageQueuer(imageURLs.toIterator, queue, new ImageDownloader(tempFolder))
          worker.start(queuer.apply)
          queue
        }
      )
    }

  def main(args: Array[String]) {
    val folder = Directory(args(0))
    Await.result(apply(folder), Duration.Inf)(folder)
  }
}
