package songs.selector

import backend.recon.ReconcilableFactory
import com.google.inject.Inject
import models.SongTagParser
import musicfinder.MusicFiles
import songs.selector.filter.{MultiStageFilterFactory, ScoreFixingMultiStageFilter}

import scala.util.Random

import common.TimedLogger
import common.path.ref.{FileRef, RefSystem}
import common.rx.RichObservable.richObservable

class MultiStageSongSelectorFactory @Inject() (
    mf: MusicFiles,
    rf: ReconcilableFactory,
    songTagParser: SongTagParser,
    multiStageFilterFactory: MultiStageFilterFactory,
    random: Random,
    timedLogger: TimedLogger,
) {
  def withSongs[Sys <: RefSystem](songs: IndexedSeq[FileRef]): MultiStageSongSelector[Sys] =
    new MultiStageSongSelector(
      songs.asInstanceOf[IndexedSeq[Sys#F]],
      rf,
      songTagParser,
      random,
      multiStageFilterFactory,
      timedLogger,
    )
  def apply(): MultiStageSongSelector[_] =
    withSongs[RefSystem](mf.getSongFiles.toVectorBlocking)
}
