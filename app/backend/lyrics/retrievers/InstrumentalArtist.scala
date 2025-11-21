package backend.lyrics.retrievers

import backend.lyrics.Instrumental
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

private[lyrics] class InstrumentalArtist @Inject() (
    ec: ExecutionContext,
    storage: InstrumentalArtistStorage,
    // Don't extend ActiveRetriever, instead, expose it as a method
) extends ActiveRetriever {
  private implicit val iec: ExecutionContext = ec
  private val helper = new DefaultInstrumentalHelper("artist")

  override def get = s => storage.contains(s.artist).map(helper.apply)
  def add(s: Song): Future[Instrumental] = storage.contains(s.artist) >| helper.instrumental
}
