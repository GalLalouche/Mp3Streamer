package backend.storage

import backend.Retriever
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

/**
 * Tries to first retrieve information from a local repository.
 * If it fails, it will try to use an online API, and save the result persistently.
 */
class OnlineRetrieverCacher[Key, Value](
    localStorage: LocalStorage[Key, Value],
    onlineRetriever: Retriever[Key, Value])
    (implicit ec: ExecutionContext) extends Retriever[Key, Value] {
  override def apply(k: Key): Future[Value] = localStorage.load(k)
      .ifNoneTry(onlineRetriever(k).flatMap(v =>
        localStorage.store(k, v)
            .filter(identity)
            .>|(v)))
}
