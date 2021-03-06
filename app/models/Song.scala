package models

import java.io.File

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
  def conductor: Option[String]
  def orchestra: Option[String]
  def opus: Option[String] // Needs to be a string to support BWV, Kochel number, etc.
  def performanceYear: Option[Int]
}

// TODO remove code duplication? hmm...
case class IOSong(file: IOFile, title: String, artistName: String, albumName: String,
    track: Int, year: Int, bitRate: String, duration: Int, size: Long,
    discNumber: Option[String], trackGain: Option[Double], composer: Option[String],
    conductor: Option[String], orchestra: Option[String], opus: Option[String], performanceYear: Option[Int],
) extends Song {
  override type F = IOFile
}
object IOSong {
  def read(f: File): IOSong = SongTagParser(f)
}

case class MemorySong(file: MemoryFile, title: String, artistName: String, albumName: String,
    track: Int, year: Int, bitRate: String, duration: Int, size: Long,
    discNumber: Option[String], trackGain: Option[Double], composer: Option[String],
    conductor: Option[String], orchestra: Option[String], opus: Option[String], performanceYear: Option[Int],
) extends Song {
  override type F = MemoryFile
}
