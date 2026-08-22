package songs.selector

import backend.recon.Artist
import com.google.inject.{Inject, Singleton}
import musicfinder.{ArtistDirsIndex, MusicFiles, SongFileFinder}
import rx.lang.scala.Observable

import common.CacheMap
import common.rich.RichFuture.richFutureBlocking
import common.rx.RichObservable.richObservable

@Singleton private class NumberOfSongsCounter @Inject() (
    mf: MusicFiles,
    sff: SongFileFinder,
    artistDirsIndex: ArtistDirsIndex,
) extends ValueCounter {
  override def apply(a: Artist): Option[Int] = cm(a)
  private val cm = new CacheMap[Artist, Option[Int]](
    artistDirsIndex
      .forArtist(_)
      .map(a =>
        mf.albumDirs(Observable.just(a)).map(sff.getSongFilesInDir(_).size).sum.firstFuture.get,
      ),
  )
}
