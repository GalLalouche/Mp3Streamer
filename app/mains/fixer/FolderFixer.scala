package mains.fixer

import java.io.File
import java.nio.file.Files

import backend.configs.StandaloneConfig
import common.rich.RichFuture._
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import mains.IOUtils
import mains.cover.DownloadCover
import models.Song

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object FolderFixer
    extends ToFunctorOps with FutureInstances {
  private implicit val c = StandaloneConfig
  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    Directory("d:/media/music")
        .deepDirs
        .find(_.name.toLowerCase == artist.toLowerCase)
  }

  private def moveDirectory(artist: String, destination: Future[Option[Directory]],
                            folderImage: Future[Directory => Unit], source: Directory): Future[Directory] = {
    val destinationParent: Future[Directory] = destination.map(_.getOrElse {
      val genreDirs = Directory("d:/media/music")
          .dirs
          .flatMap(_.dirs)
      println("Could not find artist directory... what is the artist's genre?")
      println(genreDirs.map(_.name).mkString("Genres: [", ",", "]"))
      val genre = scala.io.StdIn.readLine().toLowerCase
      genreDirs
          .find(_.name.toLowerCase == genre)
          .get
          .addSubDir(artist)
    })
    for (d <- destinationParent; f <- folderImage) yield {
      f(source)
      val dest = new File(d, source.name).toPath
      Files.move(source.toPath, dest)
      IOUtils.focus(dest.toFile)
      d
    }
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] = {
    DownloadCover(newPath) recover {
      case e: RuntimeException => println("Auto downloading picture aborted: " + e.getMessage).const
    }
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
    moveDirectory(artist, location, folderImage, fixedDirectory).map(FoobarGain.calculateTrackGain)
        .>|(println("--Done!--"))
        .get
  }
}
