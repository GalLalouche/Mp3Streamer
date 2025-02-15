package models

import scala.concurrent.duration.Duration

import common.io.FileRef

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
