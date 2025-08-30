package models

import scala.concurrent.duration.Duration

import common.io.FileRef

trait Song {
  def file: FileRef
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
}
