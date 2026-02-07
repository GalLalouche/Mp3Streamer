package songs.selector

import backend.score.{AggregateScorer, ScoreBasedProbabilityFactory}
import com.google.inject.Inject
import models.SongTagParser
import musicfinder.MusicFiles

import scala.util.Random

import common.{Filter, TimedLogger}
import common.path.ref.{FileRef, RefSystem}
import common.rx.RichObservable.richObservable

class MultiStageSongSelectorFactory @Inject() (
    mf: MusicFiles,
    songTagParser: SongTagParser,
    random: Random,
    sbpFactory: ScoreBasedProbabilityFactory,
    aggregateScorer: AggregateScorer,
    lengthFilter: LengthFilter,
    timedLogger: TimedLogger,
) {
  def withSongs[Sys <: RefSystem](songs: IndexedSeq[FileRef]): MultiStageSongSelector[Sys] =
    new MultiStageSongSelector(songs.asInstanceOf[IndexedSeq[Sys#F]])(
      mf,
      songTagParser,
      random,
      Filter.always,
      lengthFilter && new ScoreBasedFilter(random, aggregateScorer, sbpFactory(songs)),
      timedLogger,
    )
  def apply(): MultiStageSongSelector[_] =
    withSongs[RefSystem](mf.getSongFiles.toVectorBlocking)
}
