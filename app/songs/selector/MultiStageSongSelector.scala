package songs.selector

import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconcilableFactory
import models.{Song, SongTagParser}
import songs.selector.filter.MultiStageFilterFactory

import scala.annotation.tailrec
import scala.util.Random

import common.{Filter, TimedLogger}
import common.path.ref.{FileRef, RefSystem}
import common.rich.RichRandomSpecVer.richRandomSpecVer

/**
 * Can filter files, tracks, and songs. Filtering at the file and track level is much faster since
 * it doesn't require parsing the song's ID3.
 */
private class MultiStageSongSelector[Sys <: RefSystem] private[selector] (
    private val songs: IndexedSeq[Sys#F],
    private val reconcilableFactory: ReconcilableFactory,
    private val songTagParser: SongTagParser,
    private val random: Random,
    private val msff: MultiStageFilterFactory,
    private val timedLogger: TimedLogger,
    private val additionalFileFilter: Filter[Sys#F] = Filter.always,
    private val additionalSongFilter: Filter[Song] = Filter.always,
) extends ConfigurableSongSelector {
  override def randomSong(): Song = timedLogger("Selecting a random song")(randomSongImpl())

  private def randomSongImpl(): Song = {
    val filter = msff.next()
    val fileFilter = filter.fileFilter && additionalFileFilter
    val trackFilter = filter.trackFilter
    val songFilter = filter.songFilter && additionalSongFilter
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
  override def withAdditionalFileFilter(filter: Filter[FileRef]) = new MultiStageSongSelector[Sys](
    songs,
    reconcilableFactory,
    songTagParser,
    random,
    msff,
    timedLogger,
    additionalFileFilter && filter,
    additionalSongFilter,
  )
}
