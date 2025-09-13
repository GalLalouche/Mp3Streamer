package song_encoder

import java.io.File

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.{FieldKey, Tag}

import common.TagUtils._

private object TagCopier {
  def apply(input: File, output: File): Unit = {
    val fields = TagCopier.getFields(AudioFileIO.read(input).getTag)
    val audioFile = AudioFileIO.read(output)
    val outputTag = audioFile.getTag
    fields.foreach(_ set outputTag)
    audioFile.setTag(outputTag)
    audioFile.commit()
  }

  private case class Field(key: FieldKey, value: String) {
    def set(tag: Tag): Unit = tag.setField(key, value)
  }
  private def getFields(input: Tag): Iterable[Field] = {
    def copy(key: FieldKey): Option[Field] = input.firstNonEmpty(key).map(Field(key, _))
    Vector(
      FieldKey.TITLE,
      FieldKey.ARTIST,
      FieldKey.ALBUM,
      FieldKey.TRACK,
      FieldKey.YEAR,
      FieldKey.DISC_NO,
      FieldKey.REPLAYGAIN_TRACK_GAIN,
      FieldKey.REPLAYGAIN_TRACK_PEAK,
      FieldKey.COMPOSER,
      FieldKey.CONDUCTOR,
      FieldKey.ORCHESTRA,
      FieldKey.OPUS,
      FieldKey.PERFORMANCE_YEAR,
    ).flatMap(copy)
  }
}
