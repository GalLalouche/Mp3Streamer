package models

import monocle.macros.Lenses

import common.io.DirectoryRef

@Lenses
case class Album(
    dir: DirectoryRef,
    title: AlbumTitle,
    artistName: ArtistName,
    year: Int,
    songs: Seq[Song],
) {
  /**
   * Returns the property if all songs share the same property, e.g., all songs in the album have
   * the same composer. Otherwise, returns None.
   */
  def uniformProperty[A](f: Song => Option[A]): Option[A] = {
    val set = songs.map(f).toSet
    if (set.size == 1) set.head else None
  }

  def composer: Option[String] = uniformProperty(_.composer)
  def conductor: Option[String] = uniformProperty(_.conductor)
  def opus: Option[String] = uniformProperty(_.opus)
  def orchestra: Option[String] = uniformProperty(_.orchestra)
  def performanceYear: Option[Int] = uniformProperty(_.performanceYear)
}
