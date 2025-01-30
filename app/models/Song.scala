package models

import java.io.File

import scala.concurrent.duration.Duration

import common.io.{FileRef, IOFile, MemoryFile}

trait Song {
  type F <: FileRef
  def file: F
  def title: SongTitle
  def artistName: ArtistName
  def albumName: AlbumTitle
  def trackNumber: TrackNumber
  def year: Int
  def bitRate: String
  def duration: Duration
  def size: Long
  def discNumber: Option[String]
  def trackGain: Option[Double]

  def composer: Option[String]
  def conductor: Option[String]
  def orchestra: Option[String]
  def opus: Option[String] // Needs to be a string to support BWV, Kochel number, etc.
  def performanceYear: Option[Int]

  def toOptionalSong: OptionalSong = OptionalSong(
    file = file.path,
    title = Some(title),
    artistName = Some(artistName),
    albumName = Some(albumName),
    trackNumber = Some(trackNumber),
    year = Some(year),
    discNumber = discNumber,
    composer = composer,
    conductor = conductor,
    orchestra = orchestra,
    opus = opus,
    performanceYear = performanceYear,
  )
}

// TODO remove code duplication? hmm...
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
) extends Song {
  override type F = IOFile
}
object IOSong {
  def read(f: File): IOSong = SongTagParser(f)
}

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
) extends Song {
  override type F = MemoryFile
}
