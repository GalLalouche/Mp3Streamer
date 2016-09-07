package mains.fixer

import java.io.File
import java.nio.file.Files

import backend.configs.StandaloneConfig
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import mains.IOUtils
import mains.cover.DownloadCover
import models.Song

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object FolderFixer {
  private implicit val c = StandaloneConfig
  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    Directory("d:/media/music")
      .deepDirs
      .find(_.name.toLowerCase == artist.toLowerCase)
  }

  private def moveDirectory(artist: String, destination: Future[Option[Directory]],
                            folderImage: Future[Directory => Unit], source: Directory) {
    if (destination.isCompleted == false)
      println("Waiting on artist find...")
    val destinationParent: Directory = Await.result(destination, 1 minute).getOrElse {
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
    }
    Await.result(folderImage, Duration.Inf)(source)
    val dest = new File(destinationParent, source.name).toPath
    Files.move(source.toPath, dest)
    IOUtils.focus(dest.toFile)
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
    moveDirectory(artist, location, folderImage, fixedDirectory)

    println("--Done!--")
  }
}
