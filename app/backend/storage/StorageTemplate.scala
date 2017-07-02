package backend.storage

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

abstract class StorageTemplate[Key, Value](implicit ec: ExecutionContext) extends Storage[Key, Value]
    with ToFunctorOps with FutureInstances {
  /** If a previous value exists, override it. */
  protected def internalForceStore(k: Key, v: Value): Future[_]
  protected def internalDelete(k: Key): Future[_]
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    load(k).flatMap(existing => internalForceStore(k, v).>|(existing))
  override def store(k: Key, v: Value): Future[Boolean] =
    load(k)
        .map(_.isDefined)
        .flatMap(if (_) Future successful false else internalForceStore(k, v).>|(true))
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    load(k).flatMap(v => forceStore(k, v map f getOrElse default))
  override def delete(k: Key): Future[Option[Value]] =
    for(existing <- load(k); _ <- internalDelete(k)) yield existing
}
