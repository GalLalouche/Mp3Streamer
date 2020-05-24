package mains.fixer

import java.io.File
import java.util.regex.Pattern

import javax.inject.Inject
import models.{IOMusicFinder, IOSong}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.annotation.tailrec
import scala.util.Try

import common.rich.func.MoreTryInstances._
import common.rich.func.ToMoreMonadErrorOps._

import common.io.IODirectory
import common.rich.primitives.RichString._
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import common.rich.primitives.RichBoolean._

/** Fixes ID3 tags on mp3 and flac files to proper casing, delete unused tags, etc. */
private class FixLabels @Inject()(mf: IOMusicFinder) {
  private def fixFile(f: File, fixDiscNumber: Boolean): Unit = {
    val audioFile = AudioFileIO.read(f)
    val newTag = FixLabelsUtils.getFixedTag(f, fixDiscNumber, audioFile)
    audioFile.delete()
    audioFile.setTag(newTag)
    audioFile.commit()
  }

  private def newFileName(f: File): String = FixLabelsUtils.newFileName(IOSong.read(f), f.extension)

  @tailrec
  private def renameFolder(source: Directory, initialName: String): Directory = {
    val file = new File(source.parent, initialName)
    if (source.dir renameTo file) Directory(file) else renameFolder(source, initialName + "_temp")
  }

  def fix(dir: Directory): FixedDirectory = {
    def containsASingleFileWithExtension(extension: String) = dir.files.count(_.extension == extension) == 1
    require(
      (containsASingleFileWithExtension("flac") && containsASingleFileWithExtension("cue")).isFalse,
      "Folder contains an unsplit flac file; please split the file and try again.")

    dir.files.foreach(_ setWritable true) // Stupid bittorrent.
    dir.files.filter(_.extension == "m3u").foreach(_.delete)

    val ioDir = IODirectory(dir)
    val musicFiles = mf.getSongFilesInDir(ioDir).map(_.file)
    require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")

    // As opposed to 1/1–Fuck those guys.
    val hasNonTrivialDiscNumber =
      musicFiles.map(AudioFileIO.read).hasSameValues(_.getTag.getFirst(FieldKey.DISC_NO)).isFalse

    musicFiles.foreach(fixFile(_, hasNonTrivialDiscNumber))
    musicFiles.foreach(f => f renameTo new File(f.parent, newFileName(f)))

    val expectedName = {
      val (year, album) = mf.getSongsInDir(ioDir).map(_.toTuple(_.year, _.albumName)).toSet.single
      // In addition to regular file name limitations, a directory name cannot end in ".".
      s"$year ${FixLabelsUtils.validFileName(album).removeAll(FixLabels.EndingDots)}"
    }

    Try(new FixedDirectory(renameFolder(dir, expectedName), expectedName))
        .mapError(new Exception("could not rename the folder", _))
        .get
  }

  // "This should never happen" now that validFileName is used!
  def verify(dir: Directory): Boolean =
    dir.files.filter(Set("mp3", "flac") contains _.extension).forall(f => f.name == newFileName(f))
}

private object FixLabels {
  private val EndingDots = Pattern compile """\.+$"""
}
