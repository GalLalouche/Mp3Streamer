package songs

import backend.logging.Logger
import javax.inject.Inject
import models.MusicFinder
import songs.SongSelector.SongSelectorProxy

import scala.concurrent.ExecutionContext

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

//TODO move to @Provides
class SongSelectorFactory @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    logger: Logger,
) extends ToFunctorOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec
  def create(): SongSelector = {
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy(ec, mf)
    // TODO TimedFuture?
    $.update().>|(logger.info(s"SongSelector has finished updating (${
      System.currentTimeMillis() - start
    } ms)"))
    $
  }
}
