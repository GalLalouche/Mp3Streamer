package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import com.google.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.functor.ToFunctorOps

private[lyrics] class InstrumentalArtist @Inject() (
    ec: ExecutionContext,
    storage: InstrumentalArtistStorage,
) extends LyricsRetriever {
  private implicit val iec: ExecutionContext = ec
  private val helper = new DefaultInstrumentalHelper("artist")

  override def get = s => storage.exists(s.artistName).map(helper.apply)
  def add(s: Song): Future[Instrumental] = storage.store(s.artistName) >| helper.instrumental
}
