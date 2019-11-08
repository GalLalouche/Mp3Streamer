package songs

import backend.logging.Logger
import com.google.inject.Provides
import models.MusicFinder
import net.codingwell.scalaguice.ScalaModule
import songs.SongSelector.SongSelectorProxy

import scala.concurrent.ExecutionContext

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor.ToFunctorOps

import common.ModuleUtils

object SongsModule extends ScalaModule with ModuleUtils {
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
    logger.info("Song selector update starting")
    $.update().>|(logger.info(s"SongSelector has finished updating (${
      System.currentTimeMillis() - start
    } ms)"))
    $
  }
}
