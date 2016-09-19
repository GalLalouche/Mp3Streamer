package backend.storage

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

abstract class LocalStorageTemplate[Key, Value](implicit ec: ExecutionContext) extends LocalStorage[Key, Value]
    with ToFunctorOps with FutureInstances {
  /** If an existing value exists, override it. */
  protected def internalForceStore(k: Key, v: Value): Future[Unit]
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    load(k).flatMap(existing => internalForceStore(k, v).>|(existing))
  override def store(k: Key, v: Value): Future[Boolean] =
    load(k)
        .map(_.isDefined)
        .flatMap(if (_) Future successful false else internalForceStore(k, v).>|(true))
}
