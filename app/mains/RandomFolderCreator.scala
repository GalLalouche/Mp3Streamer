package mains

import java.io.File

import backend.configs.StandaloneConfig
import common.rich.RichFuture._
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.{Poster, Song}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.images.StandardArtwork

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.Random

/** Selects n random songs and puts them in a folder on D:\ */
private object RandomFolderCreator {
  implicit val c = StandaloneConfig
  private val songs = c.mf.getSongFilePaths.map(new File(_))

  private def createPlaylistFile(outputDir: Directory): Future[File] = Future {
    val files = outputDir.files
    val playlistFile = outputDir.addFile("random.m3u")
    files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  @tailrec
  private def findNewSong(existing: Set[File]): File = {
    val file = songs(random nextInt songs.length)
    if (existing contains file)
      findNewSong(existing)
    else
      file
  }

  @tailrec
  private def addSongs(maxSize: Int, forEachEntry: (File, Int) => Unit, existing: Set[File] = Set(), futures: List[Future[Unit]] = Nil): Future[Unit] = {
    val index = existing.size
    if (index == maxSize)
      return Future sequence futures map (_.reduce((_, _) => Unit))
    val newFile = findNewSong(existing)
    val f = Future apply forEachEntry(newFile, index)
    print(s"\r${100 * index / maxSize}% done")
    addSongs(maxSize, forEachEntry, existing + newFile, f :: futures)
  }

  private def copyFileToOutputDir(outputDir: Directory)(f: File, index: Int): Unit = {
    try {
      val newFile = new File(outputDir.dir, f.name)
      FileUtils.copyFile(f, newFile)
      val audioFile = AudioFileIO.read(newFile)
      audioFile.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(Song(f))))
      try {
        audioFile.commit()
      } catch {
        case e: CannotWriteException => e.printStackTrace()
        case e: UnableToRenameFileException => e.printStackTrace()
      }
      newFile.renameTo(new File(outputDir.dir, "%02d.%s".format(index, f.extension)))
    } catch {
      case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
    }
  }

  private val random = new Random
  def main(args: Array[String]): Unit = {
    val outputDir: Directory = {
      val d = Directory("D:/").addSubDir("RandomSongsOutput")
      d.clear()
      d
    }
    addSongs(300, copyFileToOutputDir(outputDir)).onEnd(createPlaylistFile(outputDir)).get
    println("\rDone!")
  }
}
