package mains.random_folder

import java.io.File

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import mains.random_folder.RandomFolderCreator.FilteredSongsDirName
import me.tongfei.progressbar.ProgressBar
import models.IOSong
import musicfinder.PosterLookup
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
import common.path.PathUtils
import common.path.ref.io.{IODirectory, IOFile, IOSystem}
import common.rich.RichFile._
import common.rich.collections.RichSeq._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._

/** Selects n random songs and dumps them in a folder on TEMP_LARGE */
private class RandomFolderCreator @Inject() (
    @Seed seed: Long,
    @Assisted songSelector: MultiStageSongSelector[IOSystem],
    posterLookup: PosterLookup,
) {
  private val random = new Random(seed)
  private val tempDirectoryName = System.getenv("TEMP_LARGE").ensuring(_ != null)

  private def createPlaylistFile(outputDir: IODirectory, name: String): File = {
    val playlistFile = outputDir.addFile(s"$name.m3u")
    outputDir.files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private def createSongSet(numberOfSongsToCreate: Int)(pb: ProgressBar): Set[File] = {
    val result = new mutable.HashSet[File]
    songSelector
      .applySetter(MultiStageSongSelector.fileFilterSetter)
      .modify(new Filter[File] {
        override def passes(a: File) = result.contains(a).isFalse
      }.&&)
    while (result.size < numberOfSongsToCreate) {
      val nextSong = songSelector.randomSong()
      result += nextSong.file.asInstanceOf[IOFile]
      pb.step()
    }
    result.toSet
  }

  private def copyFileToOutputDir(outputDir: IODirectory, pb: ProgressBar, padLength: Int)(
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
            posterLookup.getCoverArt(IOSong.read(f)).asInstanceOf[IOFile],
          ),
        )
        audioFile.commit()
      } catch {
        // Because—I wanna say Windows?—is such a piece of crap, if the folder is open while process runs,
        // committing the ID3 tag can sometimes fail.
        case e @ (_: CannotWriteException | _: UnableToRenameFileException) => e.printStackTrace()
      }
    val targetFileName =
      new File(outputDir, s"${index.padLeftZeros(padLength)}.${tempFile.extension}")
    assert(targetFileName.exists().isFalse)
    val success = tempFile.renameTo(targetFileName)
    assert(success)
    pb.step()
  } catch {
    case e: Exception => println("\rFailed @ " + f); e.printStackTrace(); throw e
  }

  private def copy(
      songs: Iterable[File],
      outputDir: IODirectory,
      playlistName: String,
  ): IODirectory = {
    assert(outputDir.deepPaths.isEmpty)
    val shuffledSongs = songs.toVector.shuffle(random)
    val padLength = shuffledSongs.size.toString.length
    for (pb <- managed(new ProgressBar(s"Copying songs", shuffledSongs.size)))
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
    copy(
      songs,
      IODirectory.makeDir(tempDirectoryName).addSubDir(outputFolder).clear(),
      playlistName,
    )
  }
  def dumpAll(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n,
    outputFolder = "RandomSongsOutput",
    playlistName = "random",
  )

  def dumpFiltered(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n,
    outputFolder = FilteredSongsDirName,
    playlistName = "running",
  )

  def moveFilteredSongs(outputName: String): IODirectory = {
    val outputDir = IODirectory.makeDir(s"$tempDirectoryName/$outputName").clear()
    // The extra files mess up the copy.
    IODirectory(s"$tempDirectoryName/$FilteredSongsDirName").files
      .filterNot(_.extensionIsAnyOf("m3u", "txt"))
      .foreach(PathUtils.move(_, outputDir))
    outputDir
  }
}

object RandomFolderCreator {
  private val FilteredSongsDirName = "Filtered Songs"
}
