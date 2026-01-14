package songs.selector

import com.google.inject.{Inject, Provider, Singleton}
import models.Song
import musicfinder.MusicFinder

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}
import common.rx.RichObservable.richObservable

// A stupid hack to make SongSelectorState lazy (since initializing all the songs takes a while) while
// remaining transparent to clients.
@Singleton class SongSelectorState @Inject() (
    mf: MusicFinder,
    fastSongSelector: FastSongSelector,
    ssFactory: Provider[MultiStageSongSelectorFactory],
    factory: UpdatableProxyFactory,
    ec: ExecutionContext,
) extends SongSelector {
  private implicit val iec: ExecutionContext = ec
  private val updater: UpdatableProxy[SongSelector] = factory(
    fastSongSelector,
    () => ssFactory.get().withSongs(mf.getSongFiles.toVectorBlocking),
  )
  def update(): Future[Unit] = updater.update().void
  update()
  override def randomSong(): Song = updater.get.randomSong()
}
