package backend.storage

import backend.Retriever
import common.rich.func.{ToMoreFunctorOps, ToMoreMonadErrorOps}
import common.storage.Storage

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
    (implicit ec: ExecutionContext) extends Retriever[Key, Value] with Storage[Key, Value]
    with ToFunctorOps with FutureInstances with ToMoreMonadErrorOps with ToMoreFunctorOps {
  override def apply(k: Key): Future[Value] =
    localStorage.load(k)
        .ifNoneTry(
          onlineRetriever(k)
              .listen(
                localStorage.store(k, _)
                    .mapError(new Exception("Cacher failed to write recon. This usually indicates a race condition.", _))))
  // delegate all methods to localStorage
  override def forceStore(k: Key, v: Value): Future[Option[Value]] = localStorage.forceStore(k, v)
  override def store(k: Key, v: Value): Future[Unit] = localStorage.store(k, v)
  override def storeMultiple(kvs: Seq[(Key, Value)]) = localStorage.storeMultiple(kvs)
  override def mapStore(k: Key, f: (Value) => Value, default: => Value): Future[Option[Value]] =
    localStorage.mapStore(k, f, default)
  override def load(k: Key): Future[Option[Value]] = localStorage.load(k)
  override def delete(k: Key): Future[Option[Value]] = localStorage.delete(k)
  override def utils = localStorage.utils
}
