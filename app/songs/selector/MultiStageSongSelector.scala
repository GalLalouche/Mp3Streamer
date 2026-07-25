package songs.selector

import backend.recon.{ReconcilableFactory, Track}
import backend.recon.Reconcilable.SongExtractor
import models.{Song, SongTagParser}
import songs.selector.MultiStageSongSelector.fileFilterSetter

import scala.annotation.tailrec
import scala.util.Random

import monocle.Monocle.toApplySetterOps
import monocle.Setter

import common.{Filter, TimedLogger}
import common.path.ref.RefSystem
import common.rich.RichRandomSpecVer.richRandomSpecVer

/**
 * Can filter files, tracks, and songs. Filtering at the file and track level is much faster since
 * it doesn't require parsing the song's ID3.
 */
class MultiStageSongSelector[Sys <: RefSystem](private val songs: IndexedSeq[Sys#F])(
    private val reconcilableFactory: ReconcilableFactory,
    private val songTagParser: SongTagParser,
    private val random: Random,
    private val fileFilter: Filter[Sys#F],
    private val trackFilter: Filter[Track],
    private val songFilter: Filter[Song],
    private val timedLogger: TimedLogger,
) extends SongSelector {
  final override def randomSong(): Song = timedLogger("Selecting a random song")(randomSongImpl())

  @tailrec private def randomSongImpl(): Song = {
    // "We don't need continues in a functional language" 🙄
    val attempt: Option[Song] = for {
      file <- Some(random.select(songs)).filter(fileFilter.passes)
      track = reconcilableFactory.tryTrack(file).toOption
      if track.forall(trackFilter.passes)
      song = songTagParser(file)
      // It is assumed that once parsed, the song filter is actually faster than the track filter.
      if songFilter.passes(song)
      // If the track is empty, we still need to verify we passed the track filter.
      if track.nonEmpty || trackFilter.passes(song.track)
    } yield song

    attempt match {
      case Some(s) => s
      case None => randomSongImpl()
    }
  }
  private def withExtensionFilter(extension: String): SongSelector = {
    val filter: Filter[Sys#F] = _.hasExtension(extension)
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
          ss.reconcilableFactory,
          ss.songTagParser,
          ss.random,
          f(ss.fileFilter),
          ss.trackFilter,
          ss.songFilter,
          ss.timedLogger,
        ),
    )
  def songFilterSetter[Sys <: RefSystem]: Setter[MultiStageSongSelector[Sys], Filter[Song]] =
    Setter[MultiStageSongSelector[Sys], Filter[Song]](f =>
      ss =>
        new MultiStageSongSelector[Sys](ss.songs)(
          ss.reconcilableFactory,
          ss.songTagParser,
          ss.random,
          ss.fileFilter,
          ss.trackFilter,
          f(ss.songFilter),
          ss.timedLogger,
        ),
    )
}
