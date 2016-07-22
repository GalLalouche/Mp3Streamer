package common.storage

import scala.concurrent.Future

trait LocalStorage[Key, Value] {
  /** Returns the previous value associated with the key, or None. */
  def forceStore(k: Key, v: Value): Future[Option[Value]]
  /** Does not override. Returns true if successful, false otherwise. */
  def store(k: Key, v: Value): Future[Boolean]
  /** Returns the value associated with the key, if one exists, or None. */
  def load(k: Key): Future[Option[Value]]
}
