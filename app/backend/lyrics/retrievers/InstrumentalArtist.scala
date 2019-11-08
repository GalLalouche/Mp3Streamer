package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor.ToFunctorOps

import common.rich.RichFuture._

private[lyrics] class InstrumentalArtist @Inject()(
    ec: ExecutionContext,
    storage: InstrumentalArtistStorage,
) extends DefaultInstrumental {
  private implicit val iec: ExecutionContext = ec

  override protected def isInstrumental(s: Song) = storage.load(s.artistName).map(_.isDefined).get
  override protected val defaultType = "artist"
  def add(s: Song): Future[Instrumental] = storage.store(s.artistName) >| instrumental
}
