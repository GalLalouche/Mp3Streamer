package songs

import backend.logging.Logger
import backend.scorer.ScoreBasedProbability
import com.google.inject.Provides
import models.MusicFinder
import net.codingwell.scalaguice.ScalaModule
import songs.SongSelector.SongSelectorProxy

import scala.concurrent.ExecutionContext

import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._

import common.guice.ModuleUtils

object SongsModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFinder]
    requireBinding[Logger]
  }

  @Provides private def songSelect(
      ec: ExecutionContext,
      mf: MusicFinder,
      scoreBasedProbability: ScoreBasedProbability,
      logger: Logger
  ): SongSelector = {
    implicit val iec: ExecutionContext = ec
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy(ec, mf, scoreBasedProbability, logger)
    // TODO TimedFuture?
    logger.info("Song selector update starting")
    $.update().>|(logger.info(s"SongSelector has finished updating (${
      System.currentTimeMillis() - start
    } ms)"))
    $
  }
}
