package songs

import backend.logging.Logger
import com.google.inject.Provides
import common.ModuleUtils
import models.MusicFinder
import net.codingwell.scalaguice.ScalaModule
import songs.SongSelector.SongSelectorProxy

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

object SongsModule extends ScalaModule with ModuleUtils
    with ToFunctorOps with FutureInstances {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFinder]
    requireBinding[Logger]
  }

  @Provides private def songSelect(
      ec: ExecutionContext,
      mf: MusicFinder,
      logger: Logger
  ): SongSelector = {
    implicit val iec: ExecutionContext = ec
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy(ec, mf)
    // TODO TimedFuture?
    logger.info("Song $selector update starting")
    $.update().>|(logger.info(s"SongSelector has finished updating (${
      System.currentTimeMillis() - start
    } ms)"))
    $
  }
}
