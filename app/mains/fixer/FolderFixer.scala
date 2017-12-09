package mains.fixer

import backend.Url
import backend.configs.{Configuration, StandaloneConfig}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFileUtils}
import mains.IOUtils
import mains.cover.DownloadCover
import models.Song

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object FolderFixer
    extends ToFunctorOps with FutureInstances {
  private implicit val c: Configuration = StandaloneConfig

  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    Directory("d:/media/music")
        .deepDirs
        .find(_.name.toLowerCase == artist.toLowerCase)
  }

  private def moveDirectory(artist: String, destination: Future[Option[Directory]],
      folderImage: Future[Directory => Unit], source: Directory,
      expectedName: String): Future[Directory] = {
    val destinationParent: Future[Directory] = destination.map(_ getOrElse NewArtistFolderCreator(artist).get)
    for (d <- destinationParent; f <- folderImage) yield {
      println("Copying folder image")
      f(source)
      val $ = RichFileUtils.move(source, d, expectedName)
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
    c.get(Url("http://localhost:9000/debug/fast_refresh"))
        .>|(println("Updated!"))
        .recover {case e => println("Failed to update server: " + e.getMessage)}
        .void
  }

  def main(args: Array[String]) {
    def extractArtistFromFile(folder: Directory): String = folder
        .files
        .filter(Set("mp3", "flac") contains _.extension)
        .head
        .mapTo(Song.apply)
        .artistName
    val folder = Directory("""E:\Incoming\Bittorrent\Completed\Music\Organized Chaos - Inner Conflict (2011)""")
    val artist = extractArtistFromFile(folder)
    val location = Future(findArtistFolder(artist))
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val (fixedDirectory, expectedName) = FixLabels fix folder.cloneDir()
    moveDirectory(artist, location, folderImage, fixedDirectory, expectedName)
        .map(FoobarGain.calculateTrackGain)
        .>|(updateServer())
        .>|(println("--Done!--"))
        .get
  }
}
