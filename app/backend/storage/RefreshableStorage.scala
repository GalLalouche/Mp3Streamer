package backend.storage

import java.time.{Clock, Duration, LocalDateTime, ZoneId}

import backend.Retriever
import backend.RichTime._
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class RefreshableStorage[Key, Value](
    freshnessStorage: FreshnessStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
    maxAge: Duration)
    (implicit ec: ExecutionContext, clock: Clock) extends Retriever[Key, Value]
    with FutureInstances with ToMoreMonadErrorOps with ToBindOps {
  private def age(dt: LocalDateTime): Duration =
    Duration.between(dt, clock.instant.atZone(ZoneId.systemDefault))
  def needsRefresh(k: Key): Future[Boolean] =
    freshnessStorage.freshness(k)
        .map(_.forall(_.exists(_.mapTo(age) > maxAge)))

  private def refresh(k: Key): Future[Value] =
    onlineRetriever(k) >>! (freshnessStorage.forceStore(k, _))
  override def apply(k: Key): Future[Value] =
    needsRefresh(k).flatMap(isOld => {
      lazy val oldData = freshnessStorage load k map (_.get)
      if (isOld) refresh(k) handleButKeepOriginal oldData.const else oldData
    })

  def withAge(k: Key): Future[(Value, Option[LocalDateTime])] = for {
      v <- apply(k)
      age <- freshnessStorage.freshness(k)
    } yield v -> age.get
}
