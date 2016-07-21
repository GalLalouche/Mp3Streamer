package common.storage

import scala.concurrent.{ExecutionContext, Future}

abstract class NewLocalStorage[Key, Value](implicit ec: ExecutionContext) extends LocalStorage[Key, Value] {
  /** If an existing value exists, override it. */
  protected def internalForceStore(k: Key, v: Value): Future[Unit]
  /** Returns the previous value associated with the key, or None. */
  def forceStore(k: Key, v: Value): Future[Option[Value]] =
    newLoad(k).flatMap(existing => internalForceStore(k, v).map(e => existing))
  /** Does not override. Returns true if successful, false otherwise. */
  def newStore(k: Key, v: Value): Future[Boolean] =
    newLoad(k)
      .map(_.isDefined)
      .flatMap(if (_) Future successful false else internalForceStore(k, v).map(e => true))
  /** Returns the value associated with the key, if one exists, or None. */
  def newLoad(k: Key): Future[Option[Value]]
  override def store(k: Key, v: Value): Future[Unit] = newStore(k, v).map(e => Unit)
  override def load(k: Key): Future[Value] = newLoad(k).map(_.get)
}
