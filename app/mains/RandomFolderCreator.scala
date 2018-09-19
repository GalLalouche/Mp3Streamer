package mains

import java.io.File

import backend.logging.Logger
import common.rich.path.Directory
import common.rich.path.RichFile.{richFile, _}
import javax.inject.Inject
import me.tongfei.progressbar.ProgressBar
import models.{IOMusicFinder, Poster, Song}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.images.StandardArtwork

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random

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

  private def copyFileToOutputDir(outputDir: Directory, pb: ProgressBar)(f: File, index: Int): Unit = {
    try {
      val newFile = new File(outputDir.dir, f.name)
      FileUtils.copyFile(f, newFile)
      val audioFile = AudioFileIO.read(newFile)
      audioFile.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(Song(f))))
      try
        audioFile.commit()
      catch {
        // Because, I wanna say Windows?, is such a piece of crap, if the folder is open while process runs,
        // committing the ID3 tag can sometimes fail.
        case e: CannotWriteException => e.printStackTrace()
        case e: UnableToRenameFileException => e.printStackTrace()
      }
      newFile.renameTo(new File(outputDir.dir, f"$index%02d.${f.extension}"))
      pb.step()
    } catch {
      case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
    }
  }
}
/** Selects n random songs and puts them in a folder on D:\ */
private object RandomFolderCreator extends {
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._
  import resource._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    val outputDir: Directory = Directory("D:/").addSubDir("RandomSongsOutput").clear()
    val logger = injector.instance[Logger]
    logger.info("Scanning for files")
    val $ = injector.instance[RandomFolderCreator]
    logger.info("Choosing songs")
    val numberOfSongsToCreate = 300
    val songs = $.createSongSet(numberOfSongsToCreate)
    for (pb <- managed(new ProgressBar("Copying songs", numberOfSongsToCreate))) {
      val copier = $.copyFileToOutputDir(outputDir, pb) _
      songs.zipWithIndex.foreach(copier.tupled)
    }
    logger.info("Creating playlist file")
    $.createPlaylistFile(outputDir)
    logger.info("Done!")
  }
}
