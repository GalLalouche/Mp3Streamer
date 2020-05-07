package mains.fixer

import java.net.ConnectException

import backend.{FutureOption, Url}
import backend.module.StandaloneModule
import com.google.inject.Guice
import javax.inject.Inject
import mains.IOUtils
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
import common.rich.primitives.RichString._
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory

class FolderFixer @Inject()(
    fixLabels: FixLabels,
    mf: IOMusicFinder,
    it: InternetTalker,
    foobarGain: FoobarGain,
    downloader: DownloadCover,
) {
  private implicit val iec: ExecutionContext = it

  private def findArtistFolder(artist: String): Option[Directory] = {
    // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
    val canonicalArtistFolderName = StringFixer(artist).toLowerCase
        // A windows folder name cannot end in '.'.
        .removeAll("""\.*$""")
        // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
        .removeAll("""[<>:"/\\|?*]""")

    println(s"finding matching folder for artist <$canonicalArtistFolderName>")
    mf.genreDirs
        .flatMap(_.deepDirs)
        .find(_.name.toLowerCase == canonicalArtistFolderName)
        .map(_.dir)
  }

  private def moveDirectory(
      artist: String,
      destination: FutureOption[Directory],
      folderImage: Future[Directory => Unit],
      fixedDirectory: Future[FixedDirectory],
  ): Future[Directory] = for {
    destinationParent <- destination |||| NewArtistFolderCreator(artist)
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
    it.get(Url("http://localhost:9000/debug/fast_refresh"))
        .>|(println("Updated!"))
        .collectHandle {
          case e: ConnectException =>
            println("Could not connect to the server, maybe it's down? " + e.getMessage)
            ()
        }.listenError(e => println("Failed to update server: " + e.getMessage))
  }

  private def run(folder: Directory): Unit = {
    val artist = mf.getSongsInDir(IODirectory(folder)).head.artistName
    val destination = OptionT(Future(findArtistFolder(artist)))
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

object FolderFixer {
  def main(args: Array[String]): Unit =
    Guice.createInjector(StandaloneModule).instance[FolderFixer].run(Directory(args(0)))
}
