package mains.random_folder

import java.io.File

import backend.scorer.ScoreBasedProbability
import com.google.inject.assistedinject.Assisted
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

import common.io.IOFile
import common.rich.collections.RichSeq._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._
import common.rich.RichRandom.richRandom

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject()(
    ec: ExecutionContext,
    mf: IOMusicFinder,
    playlistFilter: FileFilter,
    scoreBasedProbability: ScoreBasedProbability,
    @Assisted seed: Long,
) {
  private implicit val iec: ExecutionContext = ec
  private val songFileRefs = mf.getSongFiles.toIndexedSeq
  private val songFiles = songFileRefs.map(_.file)
  private val random = new Random(seed)

  private def createPlaylistFile(outputDir: Directory, name: String): File = {
    val files = outputDir.files
    val playlistFile = outputDir.addFile(s"$name.m3u")
    files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private def createSongSet(numberOfSongsToCreate: Int, filter: FileFilter)(pb: ProgressBar): Set[File] = {
    @tailrec def go(existing: Set[File]): Set[File] = {
      if (existing.size == numberOfSongsToCreate)
        return existing
      val nextFile = random.select(songFiles)
      if (filter.isAllowed(nextFile).isFalse || existing(nextFile))
        go(existing)
      else {
        val song = mf.parseSong(IOFile(nextFile))
        val next = existing.mapIf(scoreBasedProbability(song).roll(random)).to(_ + nextFile)
        if (next.size > existing.size)
          pb.step()
        go(next)
      }
    }
    go(Set())
  }

  private def copyFileToOutputDir(
      outputDir: Directory, pb: ProgressBar, padLength: Int)(
      f: File, index: Int): Unit = try {
    val tempFile = File.createTempFile("copy_file_to_output_dir", "." + f.extension)
    FileUtils.copyFile(f, tempFile)
    // Copy the file to a temporary location to avoid overriding existing files
    val audioFile = AudioFileIO.read(tempFile)
    // -- Set cover art --
    // If used on already filtered, i.e., called from copyFilteredSongs, the poster is already set.
    if (audioFile.getTag.hasField(FieldKey.COVER_ART).isFalse)
      try {
        audioFile.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(IOSong.read(f))))
        audioFile.commit()
      } catch {
        // Because—I wanna say Windows?—is such a piece of crap, if the folder is open while process runs,
        // committing the ID3 tag can sometimes fail.
        case e@(_: CannotWriteException | _: UnableToRenameFileException) => e.printStackTrace()
      }
    val targetFileName = new File(outputDir.dir, s"${index padLeftZeros padLength}.${tempFile.extension}")
    assert(targetFileName.exists().isFalse)
    tempFile.renameTo(targetFileName)
    pb.step()
  } catch {
    case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
  }

  private def copy(songs: Traversable[File], outputDir: Directory, playlistName: String): Unit = {
    assert(outputDir.deepPaths.isEmpty)
    val shuffledSongs = songs.toVector.shuffle(random)
    val padLength = shuffledSongs.size.toString.length
    for (pb <- managed(new ProgressBar("Copying songs", shuffledSongs.size)))
      shuffledSongs.zipWithIndex.foreach(Function.tupled(copyFileToOutputDir(outputDir, pb, padLength)))
    createPlaylistFile(outputDir, playlistName)
    outputDir.addFile("random_seed.txt").write(seed.toString)
  }

  private def dumpAll(
      filter: FileFilter, numberOfSongsToCreate: Int, outputFolder: String, playlistName: String): Unit = {
    val songs = managed(new ProgressBar("Choosing songs", numberOfSongsToCreate))
        .acquireAndGet(createSongSet(numberOfSongsToCreate, filter))
    assert(songs.size == numberOfSongsToCreate)
    copy(songs, Directory.makeDir("D:/").addSubDir(outputFolder).clear(), playlistName)
  }
  def dumpAll(n: Int): Unit = dumpAll(
    FileFilter.AllowEverything,
    numberOfSongsToCreate = n,
    outputFolder = "RandomSongsOutput",
    playlistName = "random",
  )

  private val FilteredSongsDirName = "Filtered Songs"
  def dumpFiltered(n: Int): Unit = dumpAll(
    playlistFilter, numberOfSongsToCreate = n, outputFolder = FilteredSongsDirName, playlistName = "running")
  def copyFilteredSongs(outputName: String = "Processed Filtered Songs", playlistName: String): Unit = {
    val dir = Directory(s"D:/$FilteredSongsDirName")
    // The extra files mess up the copy.
    dir.files.filter(Set("m3u", "txt") contains _.extension).foreach(_.delete())
    val songs = dir.files.toSet
    copy(songs, Directory.makeDir(s"D:/$outputName").clear(), playlistName)
  }
}
