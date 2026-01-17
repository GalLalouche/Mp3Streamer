package backend.new_albums.filler.storage

import java.time.{Clock, LocalDateTime, ZoneOffset}

import backend.FutureOption
import backend.new_albums.filler.storage.LastFetchTimeImpl.prependUnit
import backend.recon.{Artist, ArtistReconStorage}
import backend.storage.{ComposedFreshnessStorage, DatedFreshness, Freshness}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.implicits.{catsSyntaxApplicativeByName, catsSyntaxIfM}
import cats.syntax.flatMap.catsSyntaxFlatMapOps
import cats.syntax.functor.toFunctorOps
import common.rich.func.kats.RichOptionT._
import common.rich.func.kats.ToMoreInvariantOps.toMoreInvariantOps
import common.rich.func.kats.ToMoreMonadErrorOps._
import monocle.Iso

import common.storage.Storage

private class LastFetchTimeImpl @Inject() (
    lastFetchTimeStorage: SlickLastFetchTimeStorage,
    artistStorage: ArtistReconStorage,
    clock: Clock,
    ec: ExecutionContext,
) extends LastFetchTime {
  private implicit val iec: ExecutionContext = ec
  private val xmapped: Storage[Artist, (Unit, Freshness)] =
    lastFetchTimeStorage
      .asInstanceOf[Storage[Artist, Option[LocalDateTime]]]
      .isoMap(Freshness.iso ^<-> prependUnit)
  private val aux = new ComposedFreshnessStorage[Artist, Unit](xmapped, clock)
  override def update(a: Artist) =
    aux.update(a).void.listenError(scribe.error(s"Failed to update artist <$a>", _))
  override def ignore(a: Artist) = aux.delete(a).value >> aux.foreverFresh(a, ())
  override def unignore(a: Artist) = aux.delete(a).value >> resetToEpoch(a)
  override def freshness(a: Artist) = aux.freshness(a) ||| resetToEpochIfExists(a)
  private def resetToEpochIfExists(a: Artist): FutureOption[Freshness] =
    artistStorage.exists(a).liftSome.ifM(resetToEpoch(a).liftSome, OptionT.none)
  override def resetToEpoch(a: Artist) = {
    val go: Future[Freshness] = for {
      isReconciled <- artistStorage.exists(a)
      _ <- Future.failed(new AssertionError(s"Artist <$a> is unreconciled")).unlessA(isReconciled)
      time = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
      _ <- lastFetchTimeStorage.store(a, Some(time))
    } yield DatedFreshness(time)
    go.listenError(scribe.error(s"Failed to reset artist <$a>", _))
  }
}

private object LastFetchTimeImpl {
  private def prependUnit[A] = Iso[A, (Unit, A)](().->)(_._2)
}
