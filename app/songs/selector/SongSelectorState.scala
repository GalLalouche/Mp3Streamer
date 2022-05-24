package songs.selector

import javax.inject.{Inject, Provider}
import models.Song

import scala.concurrent.Future

// A stupid hack to make SongSelectorState lazy (since initializing all the songs takes a while) while
// remaining transparent to clients.
class SongSelectorState @Inject()(provider: Provider[EagerSongSelectorState]) extends SongSelector {
  def update(): Future[Unit] = provider.get().update()
  override def randomSong(): Song = provider.get().randomSong()
  override def followingSong(song: Song): Option[Song] = provider.get().followingSong(song)
}
