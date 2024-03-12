package models

import java.io.File

import monocle.macros.Lenses

@Lenses
case class OptionalSong(
    file: String,
    title: Option[String],
    artistName: Option[String],
    albumName: Option[AlbumTitle],
    track: Option[TrackNumber],
    year: Option[Int],
    discNumber: Option[String],
    composer: Option[String],
    conductor: Option[String],
    orchestra: Option[String],
    opus: Option[String],
    performanceYear: Option[Int],
) {
  def directory: String = new File(file).getParent
}

object OptionalSong {
  def empty(file: String): OptionalSong = OptionalSong(
    file = file,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
  )
}
