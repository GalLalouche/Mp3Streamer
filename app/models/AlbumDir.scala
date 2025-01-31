package models

import monocle.macros.Lenses

import common.io.DirectoryRef

/**
 * An album directory (contrast with [[backend.recon.Album]]) is a concrete directory containing
 * music files. In other words, an AlbumDir has to physically exist on the filesystem, as well as
 * all of its song.
 */
@Lenses
case class AlbumDir(
    dir: DirectoryRef,
    title: AlbumTitle,
    artistName: ArtistName,
    year: Int,
    songs: Seq[Song],
) {
  def composer: Option[String] = uniformProperty(_.composer)
  def conductor: Option[String] = uniformProperty(_.conductor)
  def opus: Option[String] = uniformProperty(_.opus)
  def orchestra: Option[String] = uniformProperty(_.orchestra)
  def performanceYear: Option[Int] = uniformProperty(_.performanceYear)

  // Returns the property if all songs share the same property, e.g., all songs in the album have
  // the same composer. Otherwise, returns None.
  private def uniformProperty[A](f: Song => Option[A]): Option[A] = {
    val set = songs.map(f).toSet
    if (set.size == 1) set.head else None
  }
}
