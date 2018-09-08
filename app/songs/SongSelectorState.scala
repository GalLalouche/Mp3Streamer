package songs

import com.google.inject.Provider
import common.concurrency.Extra
import javax.inject.{Inject, Singleton}
import models.Song

import scala.concurrent.Future

@Singleton
class SongSelectorState @Inject()(songSelectorProvider: Provider[SongSelector]) extends SongSelector
    with Extra {
  private var state = songSelectorProvider.get()
  private val extra = Extra {
    state = songSelectorProvider.get()
  }
  override def !(m: => Unit): Future[Unit] = extra.!()
  override def randomSong: Song = state.randomSong
  override def followingSong(song: Song): Option[Song] = state.followingSong(song)
}
