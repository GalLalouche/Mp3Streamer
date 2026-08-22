package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconcilableFactory
import models.{Song, SongTagParser}
import songs.selector.MultiStageSongSelector.fileFilterSetter
import songs.selector.filter.{MultiStageFilterFactory, ScoreFixingMultiStageFilter}

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
class MultiStageSongSelector[Sys <: RefSystem] private[selector] (
    private val songs: IndexedSeq[Sys#F],
    private val reconcilableFactory: ReconcilableFactory,
    private val songTagParser: SongTagParser,
    private val random: Random,
    private val msff: MultiStageFilterFactory,
    private val timedLogger: TimedLogger,
    private val additionalFileFilter: Filter[Sys#F] = Filter.always,
    private val additionalSongFilter: Filter[Song] = Filter.always,
) extends SongSelector {
  final override def randomSong(): Song = timedLogger("Selecting a random song")(randomSongImpl())

  private def randomSongImpl(): Song = {
    val filter = msff.next()
    val fileFilter = filter.fileFilter.&&(additionalFileFilter)
    val trackFilter = filter.trackFilter
    val songFilter = filter.songFilter.&&(additionalSongFilter)
    @tailrec def aux(): Song = {
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
        case None => aux()
      }
    }
    aux()
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
        new MultiStageSongSelector[Sys](
          ss.songs,
          ss.reconcilableFactory,
          ss.songTagParser,
          ss.random,
          ss.msff,
          ss.timedLogger,
          f(ss.additionalFileFilter),
          ss.additionalSongFilter,
        ),
    )
  def songFilterSetter[Sys <: RefSystem]: Setter[MultiStageSongSelector[Sys], Filter[Song]] =
    Setter[MultiStageSongSelector[Sys], Filter[Song]](f =>
      ss =>
        new MultiStageSongSelector[Sys](
          ss.songs,
          ss.reconcilableFactory,
          ss.songTagParser,
          ss.random,
          ss.msff,
          ss.timedLogger,
          ss.additionalFileFilter,
          f(ss.additionalSongFilter),
        ),
    )
}
