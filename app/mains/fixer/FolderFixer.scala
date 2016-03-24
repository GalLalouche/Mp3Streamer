package mains.fixer

import java.io.File
import java.nio.file.Files

import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import mains.cover.DownloadCover
import models.Song

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object FolderFixer {
  private def findArtistFolder(artist: String): Option[Directory] = {
    println("finding matching folder")
    Directory("d:/media/music")
      .deepDirs
      .find(_.name.toLowerCase == artist.toLowerCase)
  }

  private def moveDirectory(artist: String, destination: Future[Option[Directory]],
                            folderImage: Future[Directory => Unit], sourcePath: String) {
    if (destination.isCompleted == false)
      println("Waiting on artist find...")
    val destinationParent: Directory = Await.result(destination, 1 minute).getOrElse {
      val genre = readLine("Could not find artist directory... what is the artist's genre?\n").toLowerCase
      Directory("d:/media/music")
        .dirs
        .view
        .flatMap(_.dirs)
        .find(_.name.toLowerCase == genre)
        .get
        .addSubDir(artist)
    }
    val source = Directory(sourcePath)
    Await.result(folderImage, Duration.Inf)(source)
    val dest = new File(destinationParent, source.name).toPath
    Files.move(source.toPath, dest)
    new ProcessBuilder("explorer.exe", dest.toFile.getAbsolutePath).start
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] = {
    DownloadCover.apply(newPath) recover {
      case e: RuntimeException =>
        println("Auto downloading picture aborted: " + e.getMessage)
        println("Press enter to continue with the script")
        readLine()
        _ => ()
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
