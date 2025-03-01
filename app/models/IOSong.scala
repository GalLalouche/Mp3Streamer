package models

import java.io.File

import scala.concurrent.duration.Duration

import common.io.IOFile

case class IOSong(
    file: IOFile,
    title: SongTitle,
    artistName: ArtistName,
    albumName: AlbumTitle,
    trackNumber: TrackNumber,
    year: Int,
    bitRate: String,
    duration: Duration,
    size: Long,
    discNumber: Option[String],
    trackGain: Option[Double],
    composer: Option[String],
    conductor: Option[String],
    orchestra: Option[String],
    opus: Option[String],
    performanceYear: Option[Int],
) extends Song

object IOSong {
  def read(f: File): IOSong = IOSongTagParser(f)
}
