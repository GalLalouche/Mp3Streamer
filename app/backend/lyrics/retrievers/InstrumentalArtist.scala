package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import common.rich.RichFuture._
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private[lyrics] class InstrumentalArtist @Inject()(
    ec: ExecutionContext,
    storage: InstrumentalArtistStorage,
) extends DefaultInstrumental
    with FutureInstances with ToBindOps {
  private implicit val iec: ExecutionContext = ec

  override protected def isInstrumental(s: Song) =
    storage.load(s.artistName).map(_.isDefined).get
  override protected val defaultType = "Artist"
  def add(s: Song): Future[Instrumental] = storage.store(s.artistName) >> apply(s)
}
