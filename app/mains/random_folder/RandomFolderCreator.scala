package mains.random_folder

import java.io.File

import backend.scorer.{CachedModelScorer, ModelScore}
import com.google.inject.assistedinject.Assisted
import javax.inject.Inject
import mains.random_folder.RandomFolderCreator.requiredProbability
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
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._
import common.rich.RichRandom._

/** Selects n random songs and dumps them in a folder on D:\ */
private class RandomFolderCreator @Inject()(
    ec: ExecutionContext,
    mf: IOMusicFinder,
    playlistFilter: FileFilter,
    scorer: CachedModelScorer,
    @Assisted seed: Long,
) {
  private implicit val iec: ExecutionContext = ec
  private val songFileRefs = mf.getSongFiles
  private val songFiles = songFileRefs.map(_.file)
  private val random = new Random(seed)

  private val probabilities: Map[ModelScore, Double] = {
    val sum = songFileRefs.length
    val frequencies: Map[ModelScore, Int] = songFileRefs
        .map(scorer(_) getOrElse ModelScore.Default)
        .frequencies
    val unnormalized = frequencies.map {case (score, count) =>
      score -> requiredProbability(score) / (count.toDouble / sum)
    }
    val unnormalizedSum = unnormalized.values.sum
    val $ = unnormalized.mapValues(_ / unnormalizedSum)
    def baseProbability(score: ModelScore) = frequencies(score) / songFiles.size.toDouble
    def debugMessage(score: ModelScore): Unit =
      println(s"Base probability for <$score> was <${baseProbability(score)}>, " +
          s"required is ${requiredProbability(score)}")
    def assertReducedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      assert(baseProbability(score) > requiredProbability(score))
    }
    def assertIncreasedProbability(score: ModelScore): Unit = {
      debugMessage(score)
      assert(baseProbability(score) < requiredProbability(score))
    }

    assertReducedProbability(ModelScore.Crappy)
    assertReducedProbability(ModelScore.Meh)
    // Okay has no inherent bias, though it'll probably be lower to accommodate the good scores.
    debugMessage(ModelScore.Okay)
    assertIncreasedProbability(ModelScore.Good)
    assertIncreasedProbability(ModelScore.Great)
    assertIncreasedProbability(ModelScore.Amazing)
    $
  }
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
      val nextSong = songFiles(random nextInt songFiles.length)
      if (filter.isAllowed(nextSong).isFalse || existing(nextSong))
        go(existing)
      else {
        val score = scorer(IOFile(nextSong)).getOrElse(ModelScore.Default)
        val next = existing.mapIf(random.flipCoin(probabilities(score))).to(_ + nextSong)
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

private object RandomFolderCreator {
  def requiredProbability: ModelScore => Double = {
    case ModelScore.Default => requiredProbability(ModelScore.Okay)
    case ModelScore.Crappy => 0
    case ModelScore.Meh => 0.02
    case ModelScore.Okay => 0.3
    case ModelScore.Good => 0.4
    case ModelScore.Great => 0.23
    case ModelScore.Amazing => 0.13
    case ModelScore.Classic => 0.05
  }
}
