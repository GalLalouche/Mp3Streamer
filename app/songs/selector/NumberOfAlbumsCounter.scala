package songs.selector

import backend.recon.Artist
import com.google.inject.{Inject, Singleton}
import musicfinder.{ArtistDirsIndex, MusicFiles}
import rx.lang.scala.Observable

import common.CacheMap
import common.rich.RichFuture.richFutureBlocking
import common.rx.RichObservable.richObservable

@Singleton private class NumberOfAlbumsCounter @Inject() (
    mf: MusicFiles,
    artistDirsIndex: ArtistDirsIndex,
) extends ValueCounter {
  override def apply(a: Artist): Option[Int] = cm(a)

  private val cm = new CacheMap[Artist, Option[Int]](
    artistDirsIndex.forArtist(_).map(d => mf.albumDirs(Observable.just(d)).size.firstFuture.get),
  )
}
