package backend.albums.filler

import backend.albums.NewAlbum
import backend.mb.MbAlbumMetadata
import backend.recon.{Album, Artist, StringReconScorer}
import models.MusicFinder

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.RichT._

private class EagerExistingAlbums private(override val albums: Map[Artist, Set[Album]])
    extends ExistingAlbums {
  override def artists: Iterable[Artist] = albums.keys

  def removeExistingAlbums(artist: Artist, allAlbums: Seq[MbAlbumMetadata]): Seq[NewAlbumRecon] = for {
    album <- allAlbums
    // TODO this should use the album's ReconID.
    if album.isReleased && albums(artist.normalized)
        .map(_.title)
        .fornone(StringReconScorer(_, album.title) > 0.95)
  } yield NewAlbumRecon(NewAlbum.from(artist, album), album.reconId)
}

private object EagerExistingAlbums {
  def from(albums: Seq[DirectoryRef], mf: MusicFinder) = {
    new EagerExistingAlbums(albums
        .map(ExistingAlbums.toAlbum(mf))
        .groupBy(_.artist.normalized)
        .mapValues(_.toSet)
        .view.force
    )
  }

  def singleArtist(artist: Artist, mf: MusicFinder): EagerExistingAlbums = {
    val artistDir = mf.findArtistDir(artist.name).get
    EagerExistingAlbums.from(
      artistDir.dirs.mapIf(_.isEmpty).to(Vector(artistDir)),
      mf,
    )
  }
}
