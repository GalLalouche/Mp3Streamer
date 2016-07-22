package common.storage

import scala.concurrent.{ExecutionContext, Future}

abstract class LocalStorage[Key, Value](implicit ec: ExecutionContext) {
  /** If an existing value exists, override it. */
  protected def internalForceStore(k: Key, v: Value): Future[Unit]
  /** Returns the previous value associated with the key, or None. */
  def forceStore(k: Key, v: Value): Future[Option[Value]] =
    load(k).flatMap(existing => internalForceStore(k, v).map(e => existing))
  /** Does not override. Returns true if successful, false otherwise. */
  def store(k: Key, v: Value): Future[Boolean] =
    load(k)
        .map(_.isDefined)
        .flatMap(if (_) Future successful false else internalForceStore(k, v).map(e => true))
  /** Returns the value associated with the key, if one exists, or None. */
  def load(k: Key): Future[Option[Value]]
}
