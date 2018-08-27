package mains.fixer

import backend.Url
import backend.configs.{RealConfig, StandaloneConfig}
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import common.rich.path.Directory
import common.rich.path.RichFile._
import mains.IOUtils
import mains.cover.DownloadCover
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.Future

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object FolderFixer
    extends ToFunctorOps with ToMoreMonadErrorOps with FutureInstances {
  private implicit val c: RealConfig = StandaloneConfig
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]

  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    val canonicalArtistFolderName = artist.toLowerCase
        // A windows folder name cannot end in '.'.
        .replaceAll("\\.*$", "")
        // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
        .replaceAll("""[<>:"/\\|?*]""", "")

    Directory("d:/media/music")
        .deepDirs
        .find(_.name.toLowerCase == canonicalArtistFolderName)
  }

  private def moveDirectory(artist: String, destination: Future[Option[Directory]],
      folderImage: Future[Directory => Unit], fixedDirectory: FixedDirectory): Future[Directory] = {
    val destinationParent: Future[Directory] = destination.map(_ getOrElse NewArtistFolderCreator(artist).get)
    for {
      d <- destinationParent
      folderImageMover <- folderImage
    } yield {
      println("Copying folder image")
      val $ = fixedDirectory move d
      folderImageMover($)
      IOUtils focus $
      $
    }
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] = {
    DownloadCover(newPath) recover {
      case e: RuntimeException => println("Auto downloading picture aborted: " + e.getMessage).const
    }
  }

  private def updateServer(): Future[Unit] = {
    println("Updating remote server if exists...")
    it.get(Url("http://localhost:9000/debug/fast_refresh"))
        .>|(println("Updated!"))
        .listenError(e => println("Failed to update server: " + e.getMessage))
        .void
  }

  def main(args: Array[String]) {
    def extractArtistFromFile(folder: Directory): String = folder
        .files
        .filter(Set("mp3", "flac") contains _.extension)
        .head
        .mapTo(Song.apply)
        .artistName
    val folder = Directory(args(0))
    val artist = extractArtistFromFile(folder)
    val location = Future(findArtistFolder(artist))
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val fixedDirectory = FixLabels fix folder.cloneDir()
    moveDirectory(artist, location, folderImage, fixedDirectory)
        .map(FoobarGain.calculateTrackGain)
        .>|(updateServer())
        .>|(println("--Done!--"))
        .get
  }
}
