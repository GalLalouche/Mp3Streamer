package mains.fixer

import java.net.ConnectException

import backend.{FutureOption, Url}
import com.google.inject.Guice
import javax.inject.Inject
import mains.{IOUtils, MainsModule}
import mains.cover.{CoverException, DownloadCover}
import models.IOMusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOpsUnapply
import scalaz.syntax.functor.ToFunctorOps
import scalaz.OptionT
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToMoreMonadErrorOps._

import common.io.{InternetTalker, IODirectory}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory

private class FolderFixer @Inject()(
    fixLabels: FixLabels,
    mf: IOMusicFinder,
    artistFinder: ArtistFinder,
    it: InternetTalker,
    foobarGain: FoobarGain,
    downloader: DownloadCover,
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
            NewArtistFolderCreator.selectGenreDirAndPopupBrowser(artist).map(_ addSubDir StringFixer(artist))
    folderImageMover <- folderImage
    fixed <- fixedDirectory
  } yield {
    println("Copying folder image")
    fixed.move(destinationParent).<|(folderImageMover).<|(IOUtils.focus(_))
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] = downloader(newPath).recover {
    case _: CoverException => println("Auto downloading picture aborted").const
    case e: RuntimeException =>
      e.printStackTrace()
      ().const
  }

  private def updateServer(): Future[Unit] = {
    println("Updating remote server if running...")
    it.get(Url("http://localhost:9000/debug/smart_refresh"))
        .>|(println("Updated!"))
        .collectHandle {
          case e: ConnectException =>
            println("Could not connect to the server, maybe it's down? " + e.getMessage)
            ()
        }.listenError(e => println("Failed to update server: " + e.getMessage))
  }

  private def run(folder: Directory): Unit = {
    val artist = mf.getSongsInDir(IODirectory(folder)).head.artistName
    val destination = OptionT(Future(artistFinder(artist)))
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val fixedDirectory = Future(fixLabels.fix(folder.cloneDir()))
    moveDirectory(artist, destination, folderImage, fixedDirectory)
        .listen(foobarGain.apply)
        .filterWithMessage(fixLabels.verify, "Failed to rename some files!")
        .>>(updateServer())
        .>|(println("--Done!--"))
        .get
  }
}

private object FolderFixer {
  def main(args: Array[String]): Unit =
    Guice.createInjector(MainsModule).instance[FolderFixer].run(Directory(args(0)))
}
