package mains.fixer

import java.net.ConnectException

import backend.FutureOption
import better.files.FileExtensions
import com.google.inject.{Guice, Inject}
import io.lemonlabs.uri.Url
import mains.{IOUtils, MainsModule}
import mains.cover.{CoverException, DownloadCover}
import mains.fixer.FolderFixer.TempLarge
import musicfinder.{ArtistFinder, IOMusicFinder}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.OptionT
import scalaz.syntax.bind.ToBindOpsUnapply
import scalaz.syntax.functor.ToFunctorOps

import common.io.{InternetTalker, IODirectory}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.{Directory, RichFileUtils}

private[mains] class FolderFixer @Inject() private (
    fixLabels: FixLabels,
    mf: IOMusicFinder,
    artistFinder: ArtistFinder,
    it: InternetTalker,
    foobarGain: FoobarGain,
    downloader: DownloadCover,
    stringFixer: StringFixer,
) {
  private implicit val iec: ExecutionContext = it

  private def moveDirectory(
      artist: String,
      destination: FutureOption[Directory],
      folderImage: Future[Directory => Unit],
      fixedDirectory: Future[FixedDirectory],
  ): Future[Directory] = for {
    destinationParent <-
      destination ||||
        NewArtistFolderCreator
          .selectGenreDirAndPopupBrowser(artist)
          .map(_.addSubDir(stringFixer(artist)))
    folderImageMover <- folderImage
    fixed <- fixedDirectory
  } yield {
    println("Copying folder image")
    fixed.move(destinationParent).<|(folderImageMover).<|(IOUtils.focus(_))
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] =
    downloader(newPath).recover {
      case _: CoverException => println("Auto downloading picture aborted").const
      case e: RuntimeException =>
        e.printStackTrace()
        ().const
    }

  private def moveOriginalFolder(folder: Directory): Unit = {
    val target = System.getenv(TempLarge)
    require(target != null, s"Missing environment variable $TempLarge")
    println(s"Moving folder to $target")
    RichFileUtils.move(folder, Directory(target))
  }

  private def updateServer(): Future[Unit] = {
    println("Updating remote server if running...")
    it.get(Url("http://localhost:9000/debug/smart_refresh"))
      .>|(println("Updated!"))
      .collectHandle { case e: ConnectException =>
        println("Could not connect to the server, maybe it's down? " + e.getMessage)
        ()
      }
      .listenError(e => println("Failed to update server: " + e.getMessage))
  }

  def run(folder: Directory): Unit = {
    val artist = mf.getSongsInDir(IODirectory(folder)).head.artistName
    val destination = OptionT(Future(artistFinder(artist).map(_.asInstanceOf[IODirectory].dir)))
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val fixedDirectory = Future(fixLabels.fix(cloneDir(folder)))
    moveDirectory(artist, destination, folderImage, fixedDirectory)
      .listen(foobarGain.apply)
      .filterWithMessage(fixLabels.verify, "Failed to rename some files!")
      .>|(moveOriginalFolder(folder))
      .>>(updateServer())
      .>|(println("--Done!--"))
      .get
  }

  private def cloneDir(dir: Directory): Directory = {
    val newName = dir.name + "_clone"
    val newDir = dir.parent.addSubDir(newName)
    newDir.deleteAll() // delete previous directory if it exists

    better.files.File(dir.toPath).copyTo(newDir.dir.toScala, overwrite = true)
    newDir
  }
}

private[mains] object FolderFixer {
  def main(args: Array[String]): Unit =
    Guice
      .createInjector(MainsModule)
      .instance[FolderFixer]
      .run(Directory(IOUtils.decodeFile(args(0))))

  private val TempLarge = "TEMP_LARGE"
}
