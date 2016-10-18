package backend.storage

import scala.concurrent.Future

/** A SQL oriented store of key-value */
trait LocalStorage[Key, Value] {
  /** Returns the previous value associated with the key, or None. */
  def forceStore(k: Key, v: Value): Future[Option[Value]]
  /** Does not override. Returns true if successful, false otherwise. */
  def store(k: Key, v: Value): Future[Boolean]
  /**
   * If there is already a value for the supplied key, update it using the supplied function. Otherwise
   * just place the supplied value.
   * Returns the previous value, if one existed.
   */
  def mapStore(k: Key, f: Value => Value, default: => Value): Future[Option[Value]]
  /** Returns the value associated with the key, if one exists, or None. */
  def load(k: Key): Future[Option[Value]]
  def utils: LocalStorageUtils
}
