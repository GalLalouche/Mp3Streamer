package backend.storage

import java.time.{Clock, Duration, LocalDateTime, ZoneId}

import backend.Retriever

import scala.Ordering.Implicits._
import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._
import common.rich.func.ToMoreMonadErrorOps._

import common.rich.RichT._

class RefreshableStorage[Key, Value](
    freshnessStorage: FreshnessStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
    maxAge: Duration,
    clock: Clock,
)(implicit ec: ExecutionContext) extends Retriever[Key, Value] {
  private def age(dt: LocalDateTime): Duration =
    Duration.between(dt, clock.instant.atZone(ZoneId.systemDefault))
  def needsRefresh(k: Key): Future[Boolean] = freshnessStorage.freshness(k).map {
    case AlwaysFresh => false
    case DatedFreshness(dt) => age(dt) > maxAge
  } | true

  private def refresh(k: Key): Future[Value] = onlineRetriever(k) >>! (freshnessStorage.forceStore(k, _).run)
  override def apply(k: Key): Future[Value] = needsRefresh(k).flatMap {isOld =>
    lazy val oldData = freshnessStorage.load(k).get
    if (isOld) refresh(k) handleButKeepOriginal oldData.const else oldData
  }

  def withAge(k: Key): Future[(Value, Freshness)] = for {
    v <- apply(k)
    age <- freshnessStorage.freshness(k).run
  } yield v -> age.get
}
