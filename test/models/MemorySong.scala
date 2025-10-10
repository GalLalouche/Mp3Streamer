package models

import common.io.MemoryFile
import scala.concurrent.duration.Duration

case class MemorySong(
    file: MemoryFile,
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
