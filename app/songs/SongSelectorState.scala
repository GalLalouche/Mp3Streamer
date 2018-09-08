package songs

import com.google.inject.Provider
import common.concurrency.AbstractExtra
import javax.inject.{Inject, Singleton}
import models.Song

@Singleton
class SongSelectorState @Inject()(songSelectorProvider: Provider[SongSelector]) extends SongSelector
    with AbstractExtra {
  private var state = songSelectorProvider.get()
  override def randomSong: Song = state.randomSong
  override def followingSong(song: Song): Option[Song] = state.followingSong(song)
  override def apply(): Unit = state = songSelectorProvider.get()
}
