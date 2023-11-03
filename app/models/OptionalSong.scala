package models

import monocle.macros.Lenses

import java.io.File

@Lenses
case class OptionalSong(
    file: String,
    title: Option[String],
    artistName: Option[String],
    albumName: Option[String],
    track: Option[Int],
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
