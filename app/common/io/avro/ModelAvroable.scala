package common.io.avro

import java.io.File

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, IOSong, Song}
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.avro.generic.{GenericData, GenericRecord}

import scala.concurrent.duration.DurationInt

import common.io.{IOFile, PathRefFactory}
import common.io.avro.ModelAvroable.SongParser
import common.io.avro.RichAvro.{richGenericDataRecord, richGenericRecord, AvroableSeqOps}

class ModelAvroable @Inject() (
    songParser: SongParser,
    pathRefParser: PathRefFactory,
) {
  implicit def songAvroable: Avroable[Song] = new Avroable[Song] {
    override def toRecord(song: Song): GenericRecord = {
      val $ = new GenericData.Record(schema)
      $.put("file", song.file.path)
      $.put("title", song.title)
      $.put("artistName", song.artistName)
      $.put("albumName", song.albumName)
      $.put("track", song.trackNumber)
      $.put("year", song.year)
      $.put("bitrate", song.bitRate)
      $.put("duration", song.duration.toSeconds.toInt)
      $.put("size", song.size)
      $.putOpt("discNumber", song.discNumber)
      $.putOpt("trackGain", song.trackGain.map(Double.box))
      $.putOpt("composer", song.composer)
      $.putOpt("conductor", song.conductor)
      $.putOpt("orchestra", song.orchestra)
      $.putOpt("opus", song.opus)
      $.putOpt("performanceYear", song.performanceYear.map(Int.box))
      $
    }

    override def fromRecord(r: GenericRecord): Song = songParser.parse(r)
    override def schema: Schema = SongSchema
  }

  implicit def albumDirAvroable(implicit songAvroable: Avroable[Song]): Avroable[AlbumDir] =
    new Avroable[AlbumDir] {
      override def toRecord(albumDir: AlbumDir): GenericRecord = {
        val $ = new GenericData.Record(schema)
        $.put("dir", albumDir.dir.path)
        $.put("title", albumDir.title)
        $.put("artistName", albumDir.artistName)
        $.put("year", albumDir.year)
        $.put("songs", albumDir.songs.toDataArray)
        $
      }

      override def fromRecord(r: GenericRecord): AlbumDir = AlbumDir(
        dir = pathRefParser.parseDirPath(r.getString("dir")),
        title = r.getString("title"),
        artistName = r.getString("artistName"),
        year = r.getInt("year"),
        songs = r.getArray[Song]("songs").toVector,
      )

      override def schema: Schema = AlbumDirSchema
    }

  implicit def artistDirAvroable(implicit
      albumDirAvroable: Avroable[AlbumDir],
  ): Avroable[ArtistDir] = new Avroable[ArtistDir] {
    override def toRecord(artistDir: ArtistDir): GenericRecord = {
      val $ = new GenericData.Record(schema)
      $.put("dir", artistDir.dir.path)
      $.put("name", artistDir.name)
      $.put("albums", artistDir.albums.toDataArray)
      $
    }

    override def fromRecord(r: GenericRecord): ArtistDir = ArtistDir(
      dir = pathRefParser.parseDirPath(r.getString("dir")),
      name = r.getString("name"),
      _albums = r.getArray[AlbumDir]("albums").toSet,
    )

    override def schema: Schema = ArtistDirSchema
  }

  private lazy val SongSchema = SchemaBuilder
    .record("Song")
    .namespace("models")
    .fields()
    .requiredString("file")
    .requiredString("title")
    .requiredString("artistName")
    .requiredString("albumName")
    .requiredInt("track")
    .requiredInt("year")
    .requiredString("bitrate")
    .requiredInt("duration")
    .requiredLong("size")
    .optionalString("discNumber")
    .optionalDouble("trackGain")
    .optionalString("composer")
    .optionalString("conductor")
    .optionalString("orchestra")
    .optionalString("opus")
    .optionalInt("performanceYear")
    .endRecord()

  private lazy val AlbumDirSchema = SchemaBuilder
    .record("AlbumDir")
    .namespace("models")
    .fields()
    .requiredString("dir")
    .requiredString("title")
    .requiredString("artistName")
    .requiredInt("year")
    .name("songs")
    .`type`(SchemaBuilder.array().items(SongSchema))
    .noDefault()
    .endRecord()

  private lazy val ArtistDirSchema = SchemaBuilder
    .record("ArtistDir")
    .namespace("models")
    .fields()
    .requiredString("dir")
    .requiredString("name")
    .name("albums")
    .`type`(SchemaBuilder.array().items(AlbumDirSchema))
    .noDefault()
    .endRecord()
}

object ModelAvroable {
  trait SongParser {
    def parse(r: GenericRecord): Song
  }
  object IOSongAvroParser extends SongParser {
    override def parse(r: GenericRecord): IOSong = IOSong(
      file = IOFile(new File(r.getString("file"))),
      title = r.getString("title"),
      artistName = r.getString("artistName"),
      albumName = r.getString("albumName"),
      trackNumber = r.getInt("track"),
      year = r.getInt("year"),
      bitRate = r.getString("bitrate"),
      duration = r.getInt("duration").seconds,
      size = r.getLong("size"),
      discNumber = r.optString("discNumber"),
      trackGain = r.optDouble("trackGain"),
      composer = r.optString("composer"),
      conductor = r.optString("conductor"),
      orchestra = r.optString("orchestra"),
      opus = r.optString("opus"),
      performanceYear = r.optInt("performanceYear"),
    )
  }
}
