package songs.selector

import javax.inject.Inject
import models.MusicFinder

import scala.util.Random

import common.{Filter, TimedLogger}
import common.io.RefSystem

class MultiStageSongSelectorFactory @Inject()(
    musicFinder: MusicFinder,
    random: Random,
    scoreBasedFilter: ScoreBasedFilter,
    lengthFilter: LengthFilter,
    timedLogger: TimedLogger,
) {
  def withSongs[Sys <: RefSystem](songs: IndexedSeq[Sys#F]): MultiStageSongSelector[Sys] =
    new MultiStageSongSelector(
      songs)(
      musicFinder,
      random,
      Filter.always,
      lengthFilter && scoreBasedFilter,
      timedLogger,
    )
  def apply(): MultiStageSongSelector[_] = withSongs(musicFinder.getSongFiles.toVector)
}


