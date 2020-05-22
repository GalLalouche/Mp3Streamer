package backend.albums.filler

import backend.albums.NewAlbum
import backend.mb.MbAlbumMetadata
import backend.recon.{Album, Artist, StringReconScorer}
import backend.recon.Reconcilable.SongExtractor
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

private class ExistingAlbums private(val map: Map[Artist, Set[Album]]) {
  def artists: Iterable[Artist] = map.keys

  def removeExistingAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Seq[NewAlbumRecon] = for {
    album <- albums
    // TODO this should use the album's ReconID.
    if album.isReleased && map(artist.normalized)
        .map(_.title)
        .fornone(StringReconScorer(_, album.title) > 0.95)
  } yield NewAlbumRecon(NewAlbum.from(artist, album), album.reconId)
}

private object ExistingAlbums {
  def from(albums: Seq[DirectoryRef], mf: MusicFinder) = {
    def toAlbum(dir: DirectoryRef): Option[Album] = mf.getSongFilesInDir(dir)
        .headOption
        .map(firstSong =>
          if (dir.name.take(4).forall(_.isDigit)) {
            val split = dir.name.split(" ", 2).ensuring(_.length == 2)
            Album(title = split(1), year = split(0).take(4).toInt, Artist(dir.parent.name))
          } else // Single album artist
            mf.parseSong(firstSong).release
        )
    new ExistingAlbums(albums
        .flatMap(toAlbum)
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force
    )
  }

  def singleArtist(artist: Artist, mf: MusicFinder): ExistingAlbums = {
    val artistDir = mf.genreDirs
        .flatMap(_.deepDirs)
        .find(_.name.toLowerCase == artist.name.toLowerCase)
        .get
    ExistingAlbums.from(
      artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)),
      mf,
    )
  }
}
