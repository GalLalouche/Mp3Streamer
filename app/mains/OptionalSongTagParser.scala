package mains

import java.io.File

import models.SongTagParser
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import common.TagUtils.richTag
import common.rich.path.RichFile._

private object OptionalSongTagParser {
  def apply(file: File): OptionalSong = {
    val tag = AudioFileIO.read(file).getTag
    if (tag == null)
      OptionalSong.empty(file.path)
    else {
      val year = SongTagParser.extractYear(file, tag)
      OptionalSong(
        file = file.path,
        title = tag.firstNonEmpty(FieldKey.TITLE),
        artistName = tag.firstNonEmpty(FieldKey.ARTIST),
        albumName = tag.firstNonEmpty(FieldKey.ALBUM),
        trackNumber = tag.firstNonEmpty(FieldKey.TRACK).map(SongTagParser.parseTrack),
        year = year,
        discNumber = tag.firstNonEmpty(FieldKey.DISC_NO),
        composer = tag.firstNonEmpty(FieldKey.COMPOSER),
        conductor = tag.firstNonEmpty(FieldKey.CONDUCTOR),
        orchestra = tag.firstNonEmpty(FieldKey.ORCHESTRA),
        opus = tag.firstNonEmpty(FieldKey.OPUS),
        performanceYear = tag.firstNonEmpty(FieldKey.PERFORMANCE_YEAR).map(_.toInt),
      )
    }
  }

}
