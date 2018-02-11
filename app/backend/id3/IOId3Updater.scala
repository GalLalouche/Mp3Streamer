package backend.id3

import java.io.File

import common.rich.RichT._
import common.rich.path.RichFile._
import models.{IOSong, Song}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.collection.JavaConverters._

// TODO handle code duplication with FixLabels
object IOId3Updater extends Id3Updater {
  private def properTrackString(track: Int): String = if (track < 10) "0" + track else track.toString
  private sealed case class FieldUpdater(metadataExtractor: Id3Metadata => Any, fieldKey: FieldKey) {
    // Side effects galore!
    def update(data: Id3Metadata, tag: Tag): Unit = tag.setField(fieldKey, metadataExtractor(data).toString)
  }
  private object Title extends FieldUpdater(_.title, FieldKey.TITLE)
  private object ArtistName extends FieldUpdater(_.artistName, FieldKey.ARTIST)
  private object AlbumName extends FieldUpdater(_.albumName, FieldKey.ALBUM)
  private object Track extends FieldUpdater(_.track |> properTrackString, FieldKey.TRACK)
  private object Year extends FieldUpdater(_.year, FieldKey.YEAR)
  private object DiscNumber extends FieldUpdater(_.discNumber, FieldKey.DISC_NO) {
    override def update(data: Id3Metadata, tag: Tag): Unit = data.discNumber match {
      case "" => tag.deleteField(fieldKey)
      case s => tag.setField(fieldKey, s)
    }
  }
  private val Tags = List(Title, ArtistName, AlbumName, Track, Year, DiscNumber)
  private def createUpdatedTag(f: File, data: Id3Metadata, oldTag: Tag): Tag = {
    val $ = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
    oldTag.getFields.asScala foreach $.setField
    Tags.foreach(_.update(data, $))
    $
  }

  override def update(song: Song, newData: Id3Metadata): Unit = {
    val file = song.asInstanceOf[IOSong].file.file
    val audioFile = AudioFileIO read file
    val newTag = createUpdatedTag(file, newData, audioFile.getTag)
    audioFile setTag newTag
    audioFile.commit()
  }
}
