package backend.storage
import scala.collection.mutable
import scala.concurrent.Future

class MemoryBackedLocalStorage[Key, Value] extends LocalStorage[Key, Value] {
  private val map = new mutable.HashMap[Key, Value]
  override def forceStore(k: Key, v: Value): Future[Option[Value]] = Future successful map.put(k, v)
  override def store(k: Key, v: Value): Future[Boolean] =
    if (map contains k)
      Future successful false
    else {
      forceStore(k, v)
      Future successful true
    }
  override def load(k: Key): Future[Option[Value]] = Future successful map.get(k)
  def get(k: Key): Option[Value] = load(k).value.get.get
  override def utils = ???
}
