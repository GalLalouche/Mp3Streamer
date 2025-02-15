package mains.vimtag

import com.google.inject.Guice
import mains.MainsModule
import mains.fixer.{FixLabelsUtils, FolderFixer, StringFixer}
import mains.vimtag.Flag.RemoveFeat
import models.SongTagParser
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import common.TagUtils._
import common.guice.RichModule.richModule
import common.io.{DirectoryRef, IODirectory}
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce._
import common.rich.path.RichFile._
import common.rich.path.RichFileUtils
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichInt._

private object Fixer {
  def apply(dir: DirectoryRef, parsedId3: ParsedId3): Unit = {
    val ioDir = dir.asInstanceOf[IODirectory]
    val startFrom1 = parsedId3.flags(Flag.ResetTrackNumbers)
    val removeFeat = parsedId3.flags(Flag.RemoveFeat)
    val keepDiscNumber = parsedId3.flags(Flag.NoUniformDiscNo) &&
      parsedId3.songId3s.hasSameValues(_.discNumber).isFalse
    val renameFiles = parsedId3.flags(Flag.RenameFiles)
    val fixFolder = parsedId3.flags(Flag.FixFolder)
    val injector = Guice.createInjector(MainsModule.overrideWith(new ScalaModule {
      override def configure(): Unit =
        bind[StringFixer].toInstance(new StringFixer() {
          protected override val ignoreLangDetectionErrors = true
          protected override def isExemptLanguage(lang: String): Boolean =
            parsedId3.flags(Flag.Asciify).isFalse || super.isExemptLanguage(lang)
        })
    }))
    for ((individual, index) <- parsedId3.songId3s.zipWithIndex) {
      val file = ioDir.getFile(individual.relativeFileName).get.file
      val audioFile = AudioFileIO.read(file)
      val existingTag = audioFile.getTag
      val newTag = {
        val $ = if (file.extension.equalsIgnoreCase("flac")) new FlacTag else new ID3v24Tag
        def setOption(fieldKey: FieldKey, f: ParsedId3 => ParsedTag[_]): Unit =
          $.setOption(fieldKey, f(parsedId3).get(fieldKey, existingTag))

        setOption(FieldKey.ARTIST, _.artist)
        setOption(FieldKey.ALBUM, _.album)
        setOption(FieldKey.YEAR, _.year)

        setOption(FieldKey.COMPOSER, _.composer)
        setOption(FieldKey.OPUS, _.opus)
        setOption(FieldKey.CONDUCTOR, _.conductor)
        setOption(FieldKey.ORCHESTRA, _.orchestra)

        setOption(FieldKey.PERFORMANCE_YEAR, _.performanceYear)
        $.setField(FieldKey.TITLE, individual.title.mapIf(removeFeat) to RemoveFeat.removeFeat)
        $.setField(
          FieldKey.TRACK,
          (if (startFrom1) index + 1 else individual.track).padLeftZeros(2),
        )
        if (keepDiscNumber)
          $.setOption(FieldKey.DISC_NO, individual.discNumber)

        def copyTag(fieldKey: FieldKey): Unit =
          $.setOption(fieldKey, existingTag.firstNonEmpty(fieldKey))
        copyTag(FieldKey.REPLAYGAIN_TRACK_GAIN)
        copyTag(FieldKey.REPLAYGAIN_TRACK_PEAK)

        $
      }
      AudioFileIO.delete(audioFile)
      audioFile.setTag(newTag)
      audioFile.commit()
      if (fixFolder.isFalse && renameFiles) // FixFolder renames files anyway
        RichFileUtils.rename(
          file,
          injector.instance[FixLabelsUtils].newFileName(SongTagParser.apply(file), file.extension),
        )
      else if (file.parent != ioDir.dir)
        RichFileUtils.move(file, ioDir.dir)
    }
    ioDir.dir.dirs.filter(_.deepPaths.isEmpty).foreach(_.dir.delete())
    if (fixFolder)
      injector.instance[FolderFixer].run(ioDir.dir)
  }
}
