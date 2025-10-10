package backend.storage

import java.time.{Clock, Duration}

import backend.Retriever

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFlatMapOps
import common.rich.func.kats.RichOptionT._
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.rich.RichT._
import common.rich.RichTime.RichLocalDateTime

class RefreshableRetriever[Key, Value](
    freshnessStorage: FreshnessStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
    maxAge: Duration,
    clock: Clock,
)(implicit ec: ExecutionContext)
    extends Retriever[Key, Value] {
  def needsRefresh(k: Key): Future[Boolean] = freshnessStorage.freshness(k).map {
    case AlwaysFresh => false
    case DatedFreshness(dt) => dt.isOlderThan(maxAge, clock)
  } | true

  private def refresh(k: Key): Future[Value] =
    onlineRetriever(k).flatTap(freshnessStorage.update(k, _).value)
  override def apply(k: Key): Future[Value] = needsRefresh(k).flatMap { isOld =>
    lazy val oldData = freshnessStorage.load(k).get
    if (isOld) refresh(k).handleButKeepOriginal(oldData.const) else oldData
  }

  def withAge(k: Key): Future[(Value, Freshness)] = for {
    v <- apply(k)
    age <- freshnessStorage.freshness(k).value
  } yield v -> age.get
}
