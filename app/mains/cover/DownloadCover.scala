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

import scala.collection.AbstractIterator
import scala.concurrent.{ExecutionContext, Future}

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
      val query = URLEncoder.encode(s"${album.artistName} ${album.title}", "UTF-8")
      // tbm=isch => image search; tbs=iar:s => search for square images
      s"https://www.google.com/search?tbm=isch&q=$query"
    }
    val urls = imageFinder(s"${album.artistName} ${album.title}")
    for {
      locals <- LocalImageFetcher(IODirectory(albumDir))
      selection <- selectImage(locals.map(Future.successful).iterator ++ urls)
    } yield selection match {
      case Selected(img) => fileMover(img)
      case OpenBrowser =>
        BrowserUtils.pointBrowserTo(Url(searchUrl))
        // String interpolation is acting funky for some reason (will fail at runtime for unicode).
        throw new CoverException.UserOpenedBrowser
      case Cancelled => throw new CoverException.UserClosedGUI
    }
  }

  // TODO move to somewhere common
  private def filter[A](i: Iterator[Future[A]])(p: A => Boolean): Iterator[Future[A]] =
    new AbstractIterator[Future[A]] {
      override def hasNext = i.hasNext // Eh :\
      override def next() = i.next().flatMap(a => if (p(a)) Future.successful(a) else next())
    }

  private def selectImage(imageURLs: Iterator[Future[ImageSource]]): Future[ImageChoice] =
    ImageSelectionPanel(
      ImagesSupplier.withCacheAsync(
        urls = filter(imageURLs)(i => i.isSquare && i.width >= 500),
        downloader = imageDownloader.withOutput(IODirectory(DownloadCover.tempFolder)),
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
