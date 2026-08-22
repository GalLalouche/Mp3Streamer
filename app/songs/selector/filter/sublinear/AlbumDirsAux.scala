package songs.selector.filter.sublinear

import backend.recon.Artist
import com.google.inject.Inject
import musicfinder.{ArtistDirsIndex, MusicFiles}
import rx.lang.scala.Observable

import cats.implicits.catsSyntaxIfM
import common.rich.func.kats.ObservableInstances.observableInstances

import common.path.ref.DirectoryRef
import common.rich.RichFuture.richFutureBlocking
import common.rx.RichObservable.richObservable

private class AlbumDirsAux @Inject() (
    mf: MusicFiles,
    artistDirsIndex: ArtistDirsIndex,
) {
  def apply(a: Artist, f: Observable[DirectoryRef] => Observable[Int]): Option[Int] =
    artistDirsIndex
      .forArtist(a)
      .map { d =>
        val albumDirs = mf.albumDirs(Observable.just(d))
        val adjustedForSingleAlbumArtists = albumDirs.isEmpty.ifM(Observable.just(d), albumDirs)
        f(adjustedForSingleAlbumArtists).sum.firstFuture.get
      }
}
