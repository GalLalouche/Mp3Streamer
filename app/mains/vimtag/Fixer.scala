package mains.vimtag

import java.util.logging.{Level, Logger}

import common.io.DirectoryRef
import common.rich.path.RichFile._
import models.RichTag._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.FieldKey

private object Fixer {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
  def apply(dir: DirectoryRef, parsedId3: ParsedId3): Unit = for (individual <- parsedId3.songId3s) {
    val file = individual.file
    val audioFile = AudioFileIO read file
    val existingTag = audioFile.getTag
    val newTag = {
      val $ = if (file.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
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
      $.setField(FieldKey.TITLE, individual.title)
      $.setField(FieldKey.TRACK, individual.track.toString)
      $.setOption(FieldKey.DISC_NO, individual.discNumber)

      def copyTag(fieldKey: FieldKey): Unit = $.setOption(fieldKey, existingTag.firstNonEmpty(fieldKey))
      copyTag(FieldKey.REPLAYGAIN_TRACK_GAIN)
      copyTag(FieldKey.REPLAYGAIN_TRACK_PEAK)

      $
    }
    AudioFileIO delete audioFile
    audioFile setTag newTag
    audioFile.commit()
  }
}
