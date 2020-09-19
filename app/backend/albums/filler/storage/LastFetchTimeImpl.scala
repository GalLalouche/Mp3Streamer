package backend.albums.filler.storage

import java.time.{Clock, LocalDateTime, ZoneOffset}

import backend.albums.filler.storage.LastFetchTimeImpl.prependUnit
import backend.module.StandaloneModule
import backend.recon.Artist
import backend.storage.{ComposedFreshnessStorage, DatedFreshness, Freshness}
import com.google.inject.Guice
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import scalaz.Scalaz.{ToBindOps, ToFunctorOps}
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreInvariantFunctorOps._
import monocle.Iso

import common.rich.RichFuture.richFuture
import common.storage.Storage

private class LastFetchTimeImpl @Inject()(
    storage: SlickLastFetchTimeStorage,
    clock: Clock,
    ec: ExecutionContext,
) extends LastFetchTime {
  private implicit val iec: ExecutionContext = ec
  private val xmapped: Storage[Artist, (Unit, Freshness)] =
    storage.asInstanceOf[Storage[Artist, Option[LocalDateTime]]].xmapmi(Freshness.iso ^<-> prependUnit)
  private val aux = new ComposedFreshnessStorage[Artist, Unit](xmapped, clock)
  override def update(a: Artist) = aux.update(a, ()).run.void
  override def ignore(a: Artist) = aux.delete(a).run >> aux.storeWithoutTimestamp(a, ())
  override def freshness(a: Artist) = aux.freshness(a)
  override def reset(a: Artist) = {
    val time = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
    storage.delete(a).run >> storage.store(a, Some(time)) >| DatedFreshness(time)
  }
}

private object LastFetchTimeImpl {
  private def prependUnit[A] = Iso[A, (Unit, A)](().->)(_._2)

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(StandaloneModule, FillerStorageModule)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector
        .instance[SlickLastFetchTimeStorage]
        .utils.createTableIfNotExists()
        .get
  }
}

