package models

import java.io.File
import java.util.logging.{Level, Logger}

import common.io.{FileRef, IOFile, MemoryFile}

trait Song {
  type F <: FileRef
  def file: F
  def title: String
  def artistName: String
  def albumName: String
  def track: Int
  def year: Int
  def bitRate: String
  def duration: Int
  def size: Long
  def discNumber: Option[String]
  def trackGain: Option[Double]
  def composer: Option[String]
  def opus: Option[String] // Needs to be a string to support BWV, Kochel number, etc.
  def performanceYear: Option[Int]
}

// TODO remove code duplication? hmm...
case class IOSong(file: IOFile, title: String, artistName: String, albumName: String,
    track: Int, year: Int, bitRate: String, duration: Int, size: Long,
    discNumber: Option[String], trackGain: Option[Double], composer: Option[String], opus: Option[String],
    performanceYear: Option[Int]
) extends Song {
  override type F = IOFile
}

case class MemorySong(file: MemoryFile, title: String, artistName: String, albumName: String,
    track: Int, year: Int, bitRate: String, duration: Int, size: Long,
    discNumber: Option[String], trackGain: Option[Double], composer: Option[String], opus: Option[String],
    performanceYear: Option[Int]
) extends Song {
  override type F = MemoryFile
}

object Song {
  /** Parses ID3/FLAC data */
  def apply(file: File): IOSong = SongTagParser(file)
}
