package backend.storage

import backend.Retriever
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

/**
 * Tries to first retrieve information from a local repository.
 * If it fails, it will try to use an online API, and save the result persistently.
 */
class OnlineRetrieverCacher[Key, Value](
                                         localStorage: Storage[Key, Value],
                                         onlineRetriever: Retriever[Key, Value])
    (implicit ec: ExecutionContext) extends Retriever[Key, Value]
    with ToFunctorOps with FutureInstances {
  override def apply(k: Key): Future[Value] = localStorage.load(k)
      .ifNoneTry(onlineRetriever(k).flatMap(v =>
        localStorage.store(k, v)
            .filter(identity)
            .>|(v)))
}
