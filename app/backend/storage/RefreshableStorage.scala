package backend.storage

import java.time.{Clock, Duration, LocalDateTime, ZoneId}

import backend.Retriever
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps

import scala.Ordering.Implicits._
import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps
import scalaz.OptionT

class RefreshableStorage[Key, Value](
    freshnessStorage: FreshnessStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
    maxAge: Duration,
    clock: Clock,
)(implicit ec: ExecutionContext) extends Retriever[Key, Value]
    with FutureInstances with ToMoreMonadErrorOps with ToBindOps {
  private def age(dt: LocalDateTime): Duration =
    Duration.between(dt, clock.instant.atZone(ZoneId.systemDefault))
  def needsRefresh(k: Key): Future[Boolean] =
    OptionT(freshnessStorage.freshness(k)).map {
      case AlwaysFresh => false
      case DatedFreshness(dt) => age(dt) > maxAge
    } getOrElse true

  private def refresh(k: Key): Future[Value] =
    onlineRetriever(k) >>! (freshnessStorage.forceStore(k, _))
  override def apply(k: Key): Future[Value] =
    needsRefresh(k).flatMap(isOld => {
      lazy val oldData = freshnessStorage load k map (_.get)
      if (isOld) refresh(k) handleButKeepOriginal oldData.const else oldData
    })

  def withAge(k: Key): Future[(Value, Freshness)] = for {
    v <- apply(k)
    age <- freshnessStorage.freshness(k)
  } yield v -> age.get
}
