package common.storage

import common.rich.RichT._
import org.joda.time.{DateTime, Duration}

import scala.concurrent.{ExecutionContext, Future}

class RefreshableStorage[Key, Value](freshnessStorage: FreshnessStorage[Key, Value],
                                     onlineRetriever: Retriever[Key, Value],
                                     maxAge: Duration)
                                     (implicit ec: ExecutionContext) extends Retriever[Key, Value] {
  private def age(dt: DateTime): Duration = Duration.millis(DateTime.now().getMillis - dt.getMillis)
  private def needsRefresh(k: Key): Future[Boolean] =
    freshnessStorage.freshness(k)
        .map(_.map(_._2.mapTo(age).isLongerThan(maxAge)).getOrElse(true))
  
  private def refresh(k: Key): Future[Value] =
    for (v <- onlineRetriever(k);
         _ <- freshnessStorage.store(k, v))
      yield v
  override def apply(k: Key): Future[Value] =
    for (b <- needsRefresh(k);
         $ <- if (b) refresh(k) else freshnessStorage.load(k).map(_.get))
      yield $
}
