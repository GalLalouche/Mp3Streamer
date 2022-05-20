package songs

import backend.logging.Logger
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
