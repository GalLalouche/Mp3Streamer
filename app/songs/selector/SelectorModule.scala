package songs.selector

import backend.logging.Logger
import backend.scorer.CachedModelScorer
import com.google.inject.Provides
import models.{GenreFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils

private[songs] object SelectorModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFinder]
    requireBinding[Logger]
    bind[SongSelector].to[SongSelectorState]
  }

  @Provides private def lengthFilter(
      genreFinder: GenreFinder,
      cachedModelScorer: CachedModelScorer,
  ): LengthFilter =
    new LengthFilter(genreFinder = genreFinder, cachedModelScorer, minLength = 2.minutes)
}
