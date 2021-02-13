package mains.cover

import java.net.URLEncoder

import backend.Url
import com.google.inject.Guice
import javax.inject.Inject
import mains.{BrowserUtils, MainsModule}
import mains.cover.DownloadCover._
import mains.cover.image.ImageAPISearch
import models.{AlbumFactory, MusicFinder}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._

import common.concurrency.{FutureIterant, Iterant}
import common.io.{IODirectory, IOFile}
import common.rich.path.{Directory, RichFileUtils, TempDirectory}
import common.rich.path.RichFile.richFile

private[mains] class DownloadCover @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    albumFactory: AlbumFactory,
    imageFinder: ImageAPISearch,
    imageDownloader: ImageDownloader,
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
    val album = mf.getSongsInDir(IODirectory(albumDir)).head.album
    val searchUrl = {
      // TODO URIBuilder
      val query = URLEncoder.encode(s"${album.artistName} ${album.title}", "UTF-8")
      // tbm=isch => image search; tbs=iar:s => search for square images
      s"https://www.google.com/search?tbm=isch&q=$query"
    }
    val urls = imageFinder(s"${album.artistName} ${album.title}")
    val locals = LocalImageFetcher(IODirectory(albumDir))
    selectImage(locals ++ urls).map {
      case Selected(img) => fileMover(img) _
      case OpenBrowser =>
        BrowserUtils.pointBrowserTo(Url(searchUrl))
        // String interpolation is acting funky for some reason (will fail at runtime for unicode).
        throw new CoverException.UserOpenedBrowser
      case Cancelled => throw new CoverException.UserClosedGUI
    }
  }

  private def selectImage(imageURLs: FutureIterant[ImageSource]): Future[ImageChoice] =
    ImageSelectionPanel.select(
      Iterant.prefetching(imageURLs.filter(i => i.isSquare && i.width >= 500), 12)
          .flatMapF(imageDownloader.withOutput(IODirectory(DownloadCover.tempFolder)))
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
    import scalaz.std.scalaFuture.futureInstance
    import common.rich.func.ToMoreMonadErrorOps._

    import common.rich.RichFuture._
    val injector = Guice.createInjector(MainsModule)
    val folder = Directory(args mkString " ")
    println("Downloading cover image for " + folder.path)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[DownloadCover].apply(folder).collectHandle {
      case _: CoverException => _ => ()
    }.get.apply(folder)
  }
}
