package mains.cover

import javax.inject.Inject

import com.google.inject.Guice
import io.lemonlabs.uri.Url
import mains.{BrowserUtils, IOUtils, MainsModule}
import mains.cover.DownloadCover._
import mains.cover.image.ImageAPISearch
import models.AlbumDirFactory
import musicfinder.MusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._

import common.concurrency.{FutureIterant, Iterant}
import common.io.{IODirectory, IOFile}
import common.rich.RichFuture.richFuture
import common.rich.path.{Directory, RichFileUtils, TempDirectory}
import common.rich.path.RichFile.richFile

private[mains] class DownloadCover @Inject() (
    ec: ExecutionContext,
    mf: MusicFinder,
    albumFactory: AlbumDirFactory,
    imageFinder: ImageAPISearch,
    imageDownloader: ImageDownloader,
    imageSelector: ImageSelector,
) {
  import albumFactory._

  private implicit val iec: ExecutionContext = ec
  /**
   * Downloads a new image for the album.
   *
   * @param albumDir
   *   Should contain the songs. The song metadata will be used to search for the correct picture.
   * @return
   *   A future command that moves the downloaded file to the input directory and deletes all
   *   temporary files.
   */
  def apply(albumDir: Directory): Future[Directory => Unit] = {
    val album = mf.getSongsInDir(IODirectory(albumDir)).head.album
    val searchUrl = Url
      .parse("https://www.google.com/search")
      .withQueryString(
        SearchType -> ImageSearch,
        ImageType -> Square,
        "q" -> s"${album.artistName} ${album.title}",
      )
    val urls = imageFinder(s"${album.artistName} ${album.title}")
    val locals = LocalImageFetcher(IODirectory(albumDir))
    selectImage(locals ++ urls).map {
      case Selected(img) => fileMover(img) _
      case OpenBrowser =>
        BrowserUtils.pointBrowserTo(searchUrl)
        // String interpolation is acting funky for some reason (will fail at runtime for unicode).
        throw new CoverException.UserOpenedBrowser
      case Cancelled => throw new CoverException.UserClosedGUI
    }
  }

  private def selectImage(imageURLs: FutureIterant[ImageSource]): Future[ImageChoice] = {
    val sourceToImage = imageDownloader.withOutput(IODirectory(DownloadCover.tempFolder))
    imageSelector.select(
      Iterant
        .prefetching(imageURLs.filter(i => i.isSquare && i.width >= 500), 12)
        .flatMapF(
          // TODO filterSuccessful in rich whatever.
          sourceToImage(_).toTry.map(_.toOption),
        )
        .filter(_.isDefined)
        .map(_.get),
    )
  }
}

private object DownloadCover {
  private lazy val tempFolder: Directory = TempDirectory.apply()

  private val SearchType = "tbm"
  private val ImageSearch = "isch"
  private val ImageType = "tbs"
  private val Square = "iar:s"

  private def fileMover(f: FolderImage)(outputDirectory: Directory): Unit = {
    val file = f.file.asInstanceOf[IOFile].file
    if (file.parent == outputDirectory && file.name.equalsIgnoreCase("folder.jpg")) {
      RichFileUtils.rename(file, "folder.jpg") // canonize casing
      return // This can happen if a local file named folder.jpg is chosen.
    }

    f.move(outputDirectory)
  }

  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    import common.rich.func.ToMoreMonadErrorOps._
    import scalaz.std.scalaFuture.futureInstance
    val injector = Guice.createInjector(MainsModule)
    val folder = Directory(IOUtils.decodeFile(args.mkString(" ")))
    println("Downloading cover image for " + folder.path)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector
      .instance[DownloadCover]
      .apply(folder)
      .collectHandle { case _: CoverException =>
        _ => ()
      }
      .get
      .apply(folder)
  }
}
