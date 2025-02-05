package mains.random_folder

import java.io.File
import javax.inject.Inject

import com.google.inject.assistedinject.Assisted
import me.tongfei.progressbar.ProgressBar
import models.{IOSong, PosterLookup}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.StandardArtwork
import resource._
import songs.selector.MultiStageSongSelector

import scala.collection.mutable
import scala.util.Random

import monocle.Monocle.toApplySetterOps

import common.Filter
import common.io.{IOFile, IOSystem}
import common.rich.collections.RichSeq._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject() (
    @Seed seed: Long,
    @Assisted songSelector: MultiStageSongSelector[IOSystem],
    posterLookup: PosterLookup,
) {
  private val random = new Random(seed)
  private val tempDirectoryName = System.getenv("TEMP_LARGE").ensuring(_ != null)

  private def createPlaylistFile(outputDir: Directory, name: String): File = {
    val playlistFile = outputDir.addFile(s"$name.m3u")
    outputDir.files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private def createSongSet(numberOfSongsToCreate: Int)(pb: ProgressBar): Set[File] = {
    val result = new mutable.HashSet[File]
    songSelector
      .applySetter(MultiStageSongSelector.fileFilterSetter)
      .modify(new Filter[IOFile] {
        override def passes(a: IOFile) = result.contains(a.file).isFalse
      }.&&)
    while (result.size < numberOfSongsToCreate) {
      val nextSong = songSelector.randomSong()
      result += nextSong.file.asInstanceOf[IOFile].file
      pb.step()
    }
    result.toSet
  }

  private def copyFileToOutputDir(outputDir: Directory, pb: ProgressBar, padLength: Int)(
      f: File,
      index: Int,
  ): Unit = try {
    val tempFile = File.createTempFile("copy_file_to_output_dir", "." + f.extension)
    FileUtils.copyFile(f, tempFile)
    // Copy the file to a temporary location to avoid overriding existing files
    val audioFile = AudioFileIO.read(tempFile)
    // -- Set cover art --
    // If used on already filtered, i.e., called from copyFilteredSongs, the poster is already set.
    if (audioFile.getTag.hasField(FieldKey.COVER_ART).isFalse)
      try {
        audioFile.getTag.setField(
          StandardArtwork.createArtworkFromFile(
            posterLookup.getCoverArt(IOSong.read(f)).asInstanceOf[IOFile].file,
          ),
        )
        audioFile.commit()
      } catch {
        // Because—I wanna say Windows?—is such a piece of crap, if the folder is open while process runs,
        // committing the ID3 tag can sometimes fail.
        case e @ (_: CannotWriteException | _: UnableToRenameFileException) => e.printStackTrace()
      }
    val targetFileName =
      new File(outputDir.dir, s"${index.padLeftZeros(padLength)}.${tempFile.extension}")
    assert(targetFileName.exists().isFalse)
    tempFile.renameTo(targetFileName)
    pb.step()
  } catch {
    case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
  }

  private def copy(
      songs: Traversable[File],
      outputDir: Directory,
      playlistName: String,
  ): Directory = {
    assert(outputDir.deepPaths.isEmpty)
    val shuffledSongs = songs.toVector.shuffle(random)
    val padLength = shuffledSongs.size.toString.length
    for (pb <- managed(new ProgressBar("Copying songs", shuffledSongs.size)))
      shuffledSongs.zipWithIndex.foreach(
        Function.tupled(copyFileToOutputDir(outputDir, pb, padLength)),
      )
    createPlaylistFile(outputDir, playlistName)
    outputDir.addFile("random_seed.txt").write(seed.toString)
    outputDir
  }

  private def dumpAll(
      numberOfSongsToCreate: Int,
      outputFolder: String,
      playlistName: String,
  ): Unit = {
    val songs = managed(new ProgressBar("Choosing songs", numberOfSongsToCreate))
      .acquireAndGet(createSongSet(numberOfSongsToCreate))
    assert(songs.size == numberOfSongsToCreate)
    copy(songs, Directory.makeDir(tempDirectoryName).addSubDir(outputFolder).clear(), playlistName)
  }
  def dumpAll(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n,
    outputFolder = "RandomSongsOutput",
    playlistName = "random",
  )

  private val FilteredSongsDirName = "Filtered Songs"

  def dumpFiltered(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n,
    outputFolder = FilteredSongsDirName,
    playlistName = "running",
  )

  def copyFilteredSongs(
      outputName: String = "Processed Filtered Songs",
      playlistName: String,
  ): Directory = {
    val dir = Directory.makeDir(s"$tempDirectoryName/$FilteredSongsDirName")
    // The extra files mess up the copy.
    dir.files.filter(Set("m3u", "txt") contains _.extension).foreach(_.delete())
    val songs = dir.files.toSet
    copy(songs, Directory.makeDir(s"$tempDirectoryName/$outputName").clear(), playlistName)
  }
}
