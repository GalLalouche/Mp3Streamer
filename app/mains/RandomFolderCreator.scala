package mains

import java.io.File
import java.util

import backend.logging.Logger
import common.rich.collections.RichSeq._
import common.rich.path.Directory
import common.rich.path.RichFile.{richFile, _}
import javax.inject.Inject
import me.tongfei.progressbar.ProgressBar
import models.{IOMusicFinder, IOSong, Poster}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.images.StandardArtwork
import org.jaudiotagger.tag.FieldKey

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject()(ec: ExecutionContext, mf: IOMusicFinder) {
  private implicit val iec: ExecutionContext = ec
  private val songFiles = mf.getSongFiles.map(_.file)

  private def createPlaylistFile(outputDir: Directory): File = {
    val files = outputDir.files
    val playlistFile = outputDir.addFile("random.m3u")
    files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private[this] val random = new Random
  @tailrec
  private def createSongSet(numberOfSongsToCreate: Int, existing: Set[File] = Set()): Set[File] =
    if (existing.size == numberOfSongsToCreate) existing
    else createSongSet(numberOfSongsToCreate, existing + songFiles(random nextInt songFiles.length))

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
}

private object RandomFolderCreator extends {
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._
  import resource._

  private val injector = Guice createInjector StandaloneModule
  private val logger = injector.instance[Logger]
  util.logging.Logger.getLogger("org.jaudiotagger").setLevel(util.logging.Level.OFF)

  private def copy(songs: Traversable[File], outputDir: Directory): Unit = {
    val $ = injector.instance[RandomFolderCreator]
    for (pb <- managed(new ProgressBar("Copying songs", songs.size)))
      songs.toVector.shuffle.zipWithIndex.foreach(($.copyFileToOutputDir(outputDir, pb) _).tupled)
    logger.info("Creating playlist file")
    $.createPlaylistFile(outputDir)
    logger.info("Done!")
  }
  private def dumpAll(rfc: RandomFolderCreator): Unit = {
    val songs = rfc.createSongSet(numberOfSongsToCreate = 300)
    copy(songs, Directory.makeDir("D:/RandomSongsOutput").clear())
  }
  private def copyFilteredSongs(rfc: RandomFolderCreator): Unit = {
    val songs = Directory("D:/RandomSongsOutput").files.toSet
    copy(songs, Directory.makeDir("D:/Filtered Run Songs").clear())
  }
  def main(args: Array[String]): Unit = {
    logger.info("Scanning for files")
    val $ = injector.instance[RandomFolderCreator]
    logger.info("Choosing songs")
    dumpAll($)
    //copyFilteredSongs($)
  }
}
