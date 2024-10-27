package mains.cleaner.stringfixer

import javax.inject.Inject

import mains.fixer.{FixLabelsUtils, StringFixer}
import models.IOMusicFinder
import models.RichTag.richTag
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import common.io.{IODirectory, IOFile}
import common.rich.RichT._
import common.rich.path.RichFileUtils

private class TitleValidator @Inject() (
    mf: IOMusicFinder,
    sf: StringFixer,
    fixLabelsUtils: FixLabelsUtils,
) {
  //  Option(Difference(s, field = "Title", actual = s.title, expected = fixed))
  // else
  //  None
  def go(): Iterable[DirectoryAction] =
    mf.albumDirs(mf.genreDirsWithSubGenres).view.flatMap(hasDifferences)
  private def hasDifferences(dir: IODirectory): Option[DirectoryAction] =
    mf.getSongFilesInDir(dir)
      .view
      .filter { f =>
        val s = mf.parseSong(f)
        val fixed = sf(s.title)
        assert(fixed.toLowerCase == s.title.toLowerCase)
        s.title != fixed
      }
      .toVector
      .optMap(_.nonEmpty, new TitleCleaner(_, dir))
  private class TitleCleaner(files: Seq[IOFile], override val directory: IODirectory)
      extends DirectoryAction {
    override def go(): Unit =
      files.foreach(fix)
    private def fix(file: IOFile): Unit = {
      val title = mf.parseSong(file).title
      def fixTitle(): Unit = {
        val audioFile = AudioFileIO.read(file.file)
        val newTag = audioFile.getTag
        newTag.setField(
          FieldKey.TITLE,
          sf(title).ensuring(_ != newTag.firstNonEmpty(FieldKey.TITLE).get),
        )
        audioFile.delete()
        audioFile.setTag(newTag)
        audioFile.commit()
      }
      def renameFile(): Unit =
        RichFileUtils.rename(
          file.file,
          fixLabelsUtils.newFileName(mf.parseSong(file), file.extension),
        )
      fixTitle()
      renameFile()
    }
  }
}
