package mains.fixer

import backend.{FutureOption, Url}
import backend.module.StandaloneModule
import com.google.inject.Guice
import javax.inject.Inject
import mains.IOUtils
import mains.cover.DownloadCover
import models.IOMusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToMoreMonadErrorOps._

import common.io.{InternetTalker, IODirectory}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory

class FolderFixer @Inject()(
    fixLabels: FixLabels, mf: IOMusicFinder, it: InternetTalker, foobarGain: FoobarGain) {
  private implicit val iec: ExecutionContext = it

  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    val canonicalArtistFolderName = artist.toLowerCase
        // A windows folder name cannot end in '.'.
        .replaceAll("""\.*$""", "")
        // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
        .replaceAll("""[<>:"/\\|?*]""", "")

    mf.genreDirs
        .flatMap(_.deepDirs)
        .find(_.name.toLowerCase == canonicalArtistFolderName)
        .map(_.dir)
  }

  private def moveDirectory(artist: String, destination: FutureOption[Directory],
      folderImage: Future[Directory => Unit], fixedDirectory: Future[FixedDirectory]): Future[Directory] = {
    val destinationParent: Future[Directory] = destination.map(_ getOrElse NewArtistFolderCreator(artist).get)
    for {
      d <- destinationParent
      folderImageMover <- folderImage
      fixed <- fixedDirectory
    } yield {
      println("Copying folder image")
      val $ = fixed move d
      folderImageMover($)
      IOUtils focus $
      $
    }
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] = DownloadCover(newPath).recover {
    case e: RuntimeException => println("Auto downloading picture aborted: " + e.getMessage).const
  }

  private def updateServer(): Future[Unit] = {
    println("Updating remote server if running...")
    it.get(Url("http://localhost:9000/debug/fast_refresh"))
        .>|(println("Updated!"))
        .listenError(e => println("Failed to update server: " + e.getMessage))
        .void
  }

  private def run(folder: Directory): Unit = {
    val artist = mf.getSongsInDir(IODirectory(folder)).head.artistName
    val destination = Future(findArtistFolder(artist))
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val fixedDirectory = Future(fixLabels.fix(folder.cloneDir()))
    moveDirectory(artist, destination, folderImage, fixedDirectory)
        .listen(foobarGain.calculateTrackGain)
        .filterWithMessage(fixLabels.verify, "Failed to rename some files!")
        .>|(updateServer())
        .>|(println("--Done!--"))
        .get
  }
}

object FolderFixer {
  def main(args: Array[String]): Unit =
    Guice.createInjector(StandaloneModule).instance[FolderFixer].run(Directory(args(0)))
}
