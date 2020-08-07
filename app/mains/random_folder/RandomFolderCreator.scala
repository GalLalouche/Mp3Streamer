package mains.random_folder

import java.io.File

import javax.inject.Inject
import me.tongfei.progressbar.ProgressBar
import models.{IOMusicFinder, IOSong, Poster}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.images.StandardArtwork
import org.jaudiotagger.tag.FieldKey
import resource._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random

import common.rich.collections.RichSeq._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile.{richFile, _}

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject()(
    ec: ExecutionContext,
    mf: IOMusicFinder,
    runningFilter: FileFilter,
) {
  private implicit val iec: ExecutionContext = ec
  private lazy val songFiles = mf.getSongFiles.map(_.file)

  def createPlaylistFile(outputDir: Directory): File = {
    val files = outputDir.files
    val playlistFile = outputDir.addFile("random.m3u")
    files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private val random = new Random
  private def createSongSet(numberOfSongsToCreate: Int, filter: FileFilter): Set[File] = {
    @tailrec def go(existing: Set[File]): Set[File] =
      if (existing.size == numberOfSongsToCreate)
        existing
      else {
        val nextSong = songFiles(random nextInt songFiles.length)
        go(existing.mapIf(filter.isAllowed(nextSong)).to(_ + nextSong))
      }
    go(Set())
  }

  private def copyFileToOutputDir(outputDir: Directory, pb: ProgressBar)(f: File, index: Int): Unit = try {
    val newFile = new File(outputDir.dir, f.name)
    FileUtils.copyFile(f, newFile)
    val audioFile = AudioFileIO.read(newFile)
    // If used on already filtered, i.e., called from copyFilteredSongs, the poster is already set.
    if (audioFile.getTag.getFields(FieldKey.COVER_ART).isEmpty)
      try {
        audioFile.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(IOSong.read(f))))
        audioFile.commit()
      } catch {
        // Because—I wanna say Windows?—is such a piece of crap, if the folder is open while process runs,
        // committing the ID3 tag can sometimes fail.
        case e@(_: CannotWriteException | _: UnableToRenameFileException) => e.printStackTrace()
      }
    newFile.renameTo(new File(outputDir.dir, f"$index%02d.${f.extension}"))
    pb.step()
  } catch {
    case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
  }

  private def copy(songs: Traversable[File], outputDir: Directory): Unit = {
    for (pb <- managed(new ProgressBar("Copying songs", songs.size)))
      songs.toVector.shuffle.zipWithIndex.foreach((copyFileToOutputDir(outputDir, pb) _).tupled)
    createPlaylistFile(outputDir)
  }

  private def dumpAll(filter: FileFilter, numberOfSongsToCreate: Int) = {
    val songs = createSongSet(numberOfSongsToCreate, filter)
    copy(songs, Directory.makeDir("D:/RandomSongsOutput").clear())
  }
  def dumpAll(n: Int): Unit = dumpAll(FileFilter.AllowEverything, numberOfSongsToCreate = n)
  def dumpRunning(n: Int): Unit = dumpAll(runningFilter, numberOfSongsToCreate = n)
  def copyFilteredSongs(): Unit = {
    val songs = Directory("D:/RandomSongsOutput").files.toSet
    copy(songs, Directory.makeDir("D:/Filtered Run Songs").clear())
  }
}
