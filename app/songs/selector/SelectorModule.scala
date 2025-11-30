package songs.selector

import backend.score.IndividualScorer
import com.google.inject.Provides
import genre.GenreFinder
import musicfinder.MusicFinder
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import common.guice.ModuleUtils

private[songs] object SelectorModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFinder]
    bind[SongSelector].to[SongSelectorState]
  }

  @Provides private def lengthFilter(
      genreFinder: GenreFinder,
      scorer: IndividualScorer,
  ) = new LengthFilter(genreFinder, scorer, minLength = 2.minutes)
}
