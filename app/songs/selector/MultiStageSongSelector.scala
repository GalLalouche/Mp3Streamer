package songs.selector

import models.Song
import musicfinder.MusicFinder
import songs.selector.MultiStageSongSelector.fileFilterSetter

import scala.annotation.tailrec
import scala.util.Random

import monocle.Monocle.toApplySetterOps
import monocle.Setter

import common.{Filter, TimedLogger}
import common.io.RefSystem
import common.rich.RichRandom.richRandom
import common.rich.primitives.RichBoolean.richBoolean

/**
 * Can filter both files and songs. Filtering at the file level is much faster since it doesn't
 * require parsing the song's ID3.
 */
class MultiStageSongSelector[Sys <: RefSystem](private val songs: IndexedSeq[Sys#F])(
    private val musicFinder: MusicFinder,
    private val random: Random,
    private val fileFilter: Filter[Sys#F],
    private val songFilter: Filter[Song],
    private val timedLogger: TimedLogger,
) extends SongSelector {
  final override def randomSong(): Song =
    timedLogger("Selecting a random song")(randomSongImpl())

  @tailrec private def randomSongImpl(): Song = {
    val file = random.select(songs)
    if (fileFilter.passes(file).isFalse)
      randomSongImpl()
    else {
      val song = musicFinder.parseSong(file)
      if (songFilter.passes(song)) song else randomSongImpl()
    }
  }
  private def withExtensionFilter(extension: String): SongSelector = {
    val filter: Filter[Sys#F] = _.extension == extension
    this.applySetter(fileFilterSetter[Sys]).modify(filter.&&)
  }
  override def randomMp3Song(): Song = withExtensionFilter("mp3").randomSong()
  override def randomFlacSong(): Song = withExtensionFilter("flac").randomSong()
}

object MultiStageSongSelector {
  def fileFilterSetter[Sys <: RefSystem]: Setter[MultiStageSongSelector[Sys], Filter[Sys#F]] =
    Setter[MultiStageSongSelector[Sys], Filter[Sys#F]](f =>
      ss =>
        new MultiStageSongSelector[Sys](ss.songs)(
          ss.musicFinder,
          ss.random,
          f(ss.fileFilter),
          ss.songFilter,
          ss.timedLogger,
        ),
    )
  def songFilterSetter[Sys <: RefSystem]: Setter[MultiStageSongSelector[Sys], Filter[Song]] =
    Setter[MultiStageSongSelector[Sys], Filter[Song]](f =>
      ss =>
        new MultiStageSongSelector[Sys](ss.songs)(
          ss.musicFinder,
          ss.random,
          ss.fileFilter,
          f(ss.songFilter),
          ss.timedLogger,
        ),
    )
}
