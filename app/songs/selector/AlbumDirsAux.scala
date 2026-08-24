package songs.selector

import backend.recon.Artist
import com.google.inject.Inject
import musicfinder.{ArtistDirsIndex, MusicFiles}
import rx.lang.scala.Observable

import common.path.ref.DirectoryRef
import common.rich.RichFuture.richFutureBlocking
import common.rx.RichObservable.richObservable

private class AlbumDirsAux @Inject() (
    mf: MusicFiles,
    artistDirsIndex: ArtistDirsIndex,
) {
  def apply(a: Artist, f: Observable[DirectoryRef] => Observable[Int]): Option[Int] =
    artistDirsIndex.forArtist(a).map(d => f(mf.albumDirs(Observable.just(d))).sum.firstFuture.get)
}
