package songs.selector

import javax.inject.{Inject, Provider, Singleton}
import models.{MusicFinder, Song}

import scala.concurrent.Future

import common.concurrency.{UpdatableProxy, UpdatableProxyFactory}

// A stupid hack to make SongSelectorState lazy (since initializing all the songs takes a while) while
// remaining transparent to clients.
@Singleton class SongSelectorState @Inject()(
    mf: MusicFinder,
    fastSongSelector: FastSongSelector,
    ssFactory: Provider[MultiStageSongSelectorFactory],
    factory: UpdatableProxyFactory,
) extends SongSelector {
  private val updater: UpdatableProxy[SongSelector] = factory(
    fastSongSelector,
    () => ssFactory.get().withSongs(mf.getSongFiles.toVector),
  )
  def update(): Future[Unit] = updater.update()
  override def randomSong(): Song = updater.current.randomSong()
}
