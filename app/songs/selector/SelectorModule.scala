package songs.selector

import backend.logging.Logger
import com.google.inject.Provides
import mains.random_folder.{FileFilters, SongDataExtractor}
import models.{GenreFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

import java.io.File
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.guice.ModuleUtils
import common.Filter

private[songs] object SelectorModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFinder]
    requireBinding[Logger]
  }

  @Provides private def lengthFilter(
      genreFinder: GenreFinder,
  ): LengthFilter = new LengthFilter(genreFinder = genreFinder, minLength = 2.minutes)

  @Provides private def songSelector(
      ec: ExecutionContext,
      logger: Logger,
      songSelectorProxy: SongSelectorProxy
  ): SongSelector = {
    implicit val iec: ExecutionContext = ec
    val start = System.currentTimeMillis()
    // TODO TimedFuture? AsyncTimedLogger?
    logger.info("Song selector update starting")
    songSelectorProxy
        .update()
        .>|(logger.info(s"SongSelector has finished updating (${System.currentTimeMillis() - start} ms)"))
    songSelectorProxy
  }
}
