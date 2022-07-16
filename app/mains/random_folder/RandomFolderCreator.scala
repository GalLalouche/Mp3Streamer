package mains.random_folder

import com.google.inject.assistedinject.Assisted
import javax.inject.Inject
import me.tongfei.progressbar.ProgressBar
import models.{IOSong, Poster}
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{CannotWriteException, UnableToRenameFileException}
import org.jaudiotagger.tag.images.StandardArtwork
import org.jaudiotagger.tag.FieldKey
import resource._
import songs.selector.{MultiStageSongSelector, SongSelector}

import java.io.File
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.Random

import monocle.Monocle.toApplySetterOps

import common.io.{IOFile, IOSystem}
import common.rich.collections.RichSeq._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._
import common.Filter

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject()(
    ec: ExecutionContext,
    random: Random,
    @Seed seed: Long,
    @Assisted songSelector: MultiStageSongSelector[IOSystem],
) {
  private implicit val iec: ExecutionContext = ec

  private def createPlaylistFile(outputDir: Directory, name: String): File = {
    val files = outputDir.files
    val playlistFile = outputDir.addFile(s"$name.m3u")
    files.map(_.name).foreach(playlistFile.appendLine)
    playlistFile
  }

  private def createSongSet(numberOfSongsToCreate: Int)(pb: ProgressBar): Set[File] = {
    val result = new mutable.HashSet[File]
    songSelector.applySetter(MultiStageSongSelector.fileFilterSetter).modify(new Filter[IOFile] {
      override def passes(a: IOFile) = result.contains(a.file).isFalse
    }.&&)
    while (result.size < numberOfSongsToCreate) {
      val nextSong = songSelector.randomSong()
      result += nextSong.file.asInstanceOf[IOFile].file
      pb.step()
    }
    result.toSet
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

  private def copy(songs: Traversable[File], outputDir: Directory, playlistName: String): Directory = {
    assert(outputDir.deepPaths.isEmpty)
    val shuffledSongs = songs.toVector.shuffle(random)
    val padLength = shuffledSongs.size.toString.length
    for (pb <- managed(new ProgressBar("Copying songs", shuffledSongs.size)))
      shuffledSongs.zipWithIndex.foreach(Function.tupled(copyFileToOutputDir(outputDir, pb, padLength)))
    createPlaylistFile(outputDir, playlistName)
    outputDir.addFile("random_seed.txt").write(seed.toString)
    outputDir
  }

  private def dumpAll(numberOfSongsToCreate: Int, outputFolder: String, playlistName: String): Unit = {
    val songs = managed(new ProgressBar("Choosing songs", numberOfSongsToCreate))
        .acquireAndGet(createSongSet(numberOfSongsToCreate))
    assert(songs.size == numberOfSongsToCreate)
    copy(songs, Directory.makeDir("D:/").addSubDir(outputFolder).clear(), playlistName)
  }
  def dumpAll(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n,
    outputFolder = "RandomSongsOutput",
    playlistName = "random",
  )

  private val FilteredSongsDirName = "Filtered Songs"

  def dumpFiltered(n: Int): Unit = dumpAll(
    numberOfSongsToCreate = n, outputFolder = FilteredSongsDirName, playlistName = "running")

  def copyFilteredSongs(outputName: String = "Processed Filtered Songs", playlistName: String): Directory = {
    val dir = Directory(s"D:/$FilteredSongsDirName")
    // The extra files mess up the copy.
    dir.files.filter(Set("m3u", "txt") contains _.extension).foreach(_.delete())
    val songs = dir.files.toSet
    copy(songs, Directory.makeDir(s"D:/$outputName").clear(), playlistName)
  }
}
