package backend.storage

import java.time.{Clock, Duration, LocalDateTime, ZoneId}

import backend.Retriever
import common.rich.RichFuture._
import common.rich.RichT._
import backend.RichTime._

import scala.concurrent.{ExecutionContext, Future}

class RefreshableStorage[Key, Value](
    freshnessStorage: FreshnessStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
    maxAge: Duration)
    (implicit ec: ExecutionContext, clock: Clock) extends Retriever[Key, Value] {
  private def age(dt: LocalDateTime): Duration =
    Duration.between(dt, clock.instant.atZone(ZoneId.systemDefault))
  def needsRefresh(k: Key): Future[Boolean] =
    freshnessStorage.freshness(k)
        .map(_.forall(_.exists(_.mapTo(age) > maxAge)))
  private def refresh(k: Key): Future[Value] =
    for (v <- onlineRetriever(k);
         _ <- freshnessStorage.forceStore(k, v))
      yield v

  override def apply(k: Key): Future[Value] =
    needsRefresh(k).flatMap(isOld => {
      lazy val oldData = freshnessStorage.load(k).map(_.get)
      if (isOld)
        refresh(k) orElseTry oldData
      else
        oldData
    })

  def withAge(k: Key): Future[(Value, Option[LocalDateTime])] =
    for (v <- apply(k); age <- freshnessStorage.freshness(k)) yield v -> age.get
}
