package mains.cover

import java.net.URLEncoder

import backend.Url
import backend.logging.Logger
import backend.module.StandaloneModule
import com.google.inject.Guice
import javax.inject.Inject
import mains.cover.DownloadCover._
import mains.cover.image.ImageFinder
import models.{AlbumFactory, MusicFinder}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.Process

import common.io.{IODirectory, IOFile}
import common.rich.path.{Directory, RichFileUtils, TempDirectory}
import common.rich.path.RichFile.richFile
import common.rich.primitives.RichString._
import common.rich.primitives.RichTry._

private[mains] class DownloadCover @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    albumFactory: AlbumFactory,
    imageFinder: ImageFinder,
    imageDownloader: ImageDownloader,
    logger: Logger,
) {
  import albumFactory._

  private implicit val iec: ExecutionContext = ec

  /**
   * Downloads a new image for the album.
   *
   * @param albumDir Should contain the songs. The song metadata will be used to search for the correct picture.
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
      urls <- imageFinder.find(Url(searchUrl)).map(_.getOrElseF {e =>
        logger.error("Could not fetch images from google", e)
        Nil
      })
      locals <- LocalImageFetcher(IODirectory(albumDir))
      selection <- selectImage(locals ++ urls)
    } yield selection match {
      case Selected(img) => fileMover(img)
      case OpenBrowser =>
        // String interpolation is acting funky for some reason (will fail at runtime for unicode).
        Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe """ + searchUrl.quote).!!
        throw CoverException("User opened browser")
      case Cancelled => throw CoverException("User opted out")
    }
  }

  private def selectImage(imageURLs: Seq[ImageSource]): Future[ImageChoice] = ImageSelectionPanel(
    ImagesSupplier.withCache(
      imageURLs.iterator.filter(i => i.isSquare && i.width >= 500),
      imageDownloader.withOutput(IODirectory(DownloadCover.tempFolder)),
      cacheSize = 12,
    )
  )
}

private object DownloadCover {
  private lazy val tempFolder: Directory = TempDirectory.apply()

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

  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    val injector = Guice.createInjector(StandaloneModule)
    val folder = Directory(args mkString " ")
    println("Downloading cover image for " + folder.path)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[DownloadCover].apply(folder).get.apply(folder)
  }
}
