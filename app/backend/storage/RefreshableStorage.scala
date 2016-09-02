package backend.storage

import backend.Retriever
import common.rich.RichT._
import org.joda.time.{DateTime, Duration}

import scala.concurrent.{ExecutionContext, Future}

class RefreshableStorage[Key, Value](freshnessStorage: FreshnessStorage[Key, Value],
                                     onlineRetriever: Retriever[Key, Value],
                                     maxAge: Duration)
                                    (implicit ec: ExecutionContext) extends Retriever[Key, Value] {
  private def age(dt: DateTime): Duration = Duration.millis(DateTime.now().getMillis - dt.getMillis)
  def needsRefresh(k: Key): Future[Boolean] =
    freshnessStorage.freshness(k)
        .map(_.forall(_.exists(_.mapTo(age).isLongerThan(maxAge))))
  private def refresh(k: Key): Future[Value] =
    for (v <- onlineRetriever(k);
         _ <- freshnessStorage.forceStore(k, v))
      yield v

  override def apply(k: Key): Future[Value] =
    for (b <- needsRefresh(k);
         $ <- if (b) refresh(k) else freshnessStorage.load(k).map(_.get))
      yield $

  def withAge(k: Key): Future[(Value, Option[DateTime])] =
    for (v <- apply(k); age <- freshnessStorage.freshness(k)) yield v -> age.get
}
