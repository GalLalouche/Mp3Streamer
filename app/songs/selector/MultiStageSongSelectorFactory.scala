package songs.selector

import com.google.inject.Inject
import models.SongTagParser
import musicfinder.MusicFinder

import scala.util.Random

import common.{Filter, TimedLogger}
import common.io.{FileRef, RefSystem}
import common.rx.RichObservable.richObservable

class MultiStageSongSelectorFactory @Inject() (
    musicFinder: MusicFinder,
    songTagParser: SongTagParser,
    random: Random,
    scoreBasedFilter: ScoreBasedFilter,
    lengthFilter: LengthFilter,
    timedLogger: TimedLogger,
) {
  def withSongs[Sys <: RefSystem](songs: IndexedSeq[FileRef]): MultiStageSongSelector[Sys] =
    new MultiStageSongSelector(songs.asInstanceOf[IndexedSeq[Sys#F]])(
      musicFinder,
      songTagParser,
      random,
      Filter.always,
      lengthFilter && scoreBasedFilter,
      timedLogger,
    )
  def apply(): MultiStageSongSelector[_] =
    withSongs[RefSystem](musicFinder.getSongFiles.toVectorBlocking)
}
