package mains.fixer

import java.io.File
import java.util.regex.Pattern

import com.google.inject.Inject
import models.IOSong
import musicfinder.{IOSongFileFinder, SongDirectoryParser}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.annotation.tailrec
import scala.util.Try

import common.rich.func.kats.ToMoreMonadErrorOps._

import common.path.ref.io.{IODirectory, IOFile}
import common.rich.RichFile.richFile
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

/** Fixes ID3 tags on mp3 and flac files to proper casing, delete unused tags, etc. */
private class FixLabels @Inject() (
    songDirParser: SongDirectoryParser,
    fixLabelsUtils: FixLabelsUtils,
    sff: IOSongFileFinder,
) {
  private def fixFile(f: IOFile, fixDiscNumber: Boolean): Unit = {
    val audioFile = AudioFileIO.read(f)
    val newTag = fixLabelsUtils.getFixedTag(f, audioFile, fixDiscNumber)
    audioFile.delete()
    audioFile.setTag(newTag)
    audioFile.commit()
  }

  private def newFileName(f: File): String = fixLabelsUtils.newFileName(IOSong.read(f), f.extension)

  @tailrec
  private def renameFolder(source: IODirectory, initialName: String): IODirectory = {
    val file = new File(source.parent, initialName)
    if (source.renameTo(file)) IODirectory(file)
    else renameFolder(source, initialName + "_temp")
  }

  def fix(ioDir: IODirectory): FixedDirectory = {
    def containsASingleFileWithExtension(extension: String) =
      ioDir.files.count(_.hasExtension(extension)) == 1
    require(
      (containsASingleFileWithExtension("flac") && containsASingleFileWithExtension("cue")).isFalse,
      "Folder contains an unsplit flac file; please split the file and try again.",
    )

    ioDir.files.foreach(_.setWritable(true)) // Stupid bittorrent.
    ioDir.files.filter(_.hasExtension("m3u")).foreach(_.delete)

    val musicFiles = sff.getSongFilesInDir(ioDir).toVector
    require(
      musicFiles.nonEmpty,
      s"Could not find any songs in $ioDir - maybe they're in subfolders...",
    )

    // As opposed to 1/1–Fuck those guys.
    val hasNonTrivialDiscNumber =
      musicFiles.map(AudioFileIO.read).hasSameValues(_.getTag.getFirst(FieldKey.DISC_NO)).isFalse

    musicFiles.foreach(fixFile(_, fixDiscNumber = hasNonTrivialDiscNumber))
    musicFiles.foreach(f =>
      if (f.renameTo(new File(f.parent, newFileName(f))).isFalse)
        scribe.warn(s"Could not rename file ${f.path} to ${newFileName(f)}"),
    )

    val expectedName = {
      val (year, album) = songDirParser(ioDir).map(_.toTuple(_.year, _.albumName)).toSet.single
      // In addition to regular file name limitations, a directory name cannot end in ".".
      s"$year ${fixLabelsUtils.validFileName(album).removeAll(FixLabels.EndingDots)}"
    }

    Try(new FixedDirectory(renameFolder(ioDir, expectedName), expectedName))
      .mapError(new Exception("could not rename the folder", _))
      .get
  }

  // "This should never happen" now that validFileName is used!
  def verify(dir: IODirectory): Boolean =
    sff.getSongFilesInDir(dir).forall(f => f.name == newFileName(f))
}

private object FixLabels {
  private val EndingDots = Pattern.compile("""\.+$""")
}
