package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import com.google.inject.Inject
import musicfinder.ArtistDirsIndex
import rx.lang.scala.Observable

import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.primitives.RichOption.richOption
import common.rx.RichObservable.richObservable

private class PreCachedExistingAlbumsFactory @Inject() (
    artistDirsIndex: ArtistDirsIndex,
    reconcilableFactory: ReconcilableFactory,
) {
  def from(albums: Observable[DirectoryRef]) = new PreCachedExistingAlbums(
    albums
      .flatMap(d => Observable.from(toAlbum(d).asIterable))
      .toMultiMapBlocking(_.artist)(_.toSet),
  )

  private def toAlbum(dir: DirectoryRef): Option[Album] =
    reconcilableFactory
      .toAlbum(dir)
      .<|(_.left.foreach(e => scribe.error(s"Failed to convert <$dir> to an album due to <$e>")))
      .toOption
  def singleArtist(artist: Artist): PreCachedExistingAlbums = {
    val artistDir: DirectoryRef = artistDirsIndex
      .forArtist(artist)
      .getOrThrow(s"Could not find directory for artist <${artist.name}>")
    from(
      if (artistDir.dirs.isEmpty) Observable.just(artistDir)
      else Observable.from(artistDir.dirs.toVector),
    )
  }
}
