package mains

import java.io.File

import models.{AlbumTitle, ArtistName, Song, TrackNumber}

import monocle.macros.Lenses

@Lenses
private case class OptionalSong(
    file: String,
    title: Option[String],
    artistName: Option[ArtistName],
    albumName: Option[AlbumTitle],
    trackNumber: Option[TrackNumber],
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

private object OptionalSong {
  def empty(file: String): OptionalSong =
    OptionalSong(file = file, None, None, None, None, None, None, None, None, None, None, None)

  def from(song: Song): OptionalSong = OptionalSong(
    file = song.file.path,
    title = Some(song.title),
    artistName = Some(song.artistName),
    albumName = Some(song.albumName),
    trackNumber = Some(song.trackNumber),
    year = Some(song.year),
    discNumber = song.discNumber,
    composer = song.composer,
    conductor = song.conductor,
    orchestra = song.orchestra,
    opus = song.opus,
    performanceYear = song.performanceYear,
  )
}
