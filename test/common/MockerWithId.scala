package common

import org.mockito.Mockito

import scala.collection.mutable

import common.rich.primitives.RichBoolean._

final class MockerWithId {
  private val map = new mutable.HashMap[Any, Any]()
  def apply[T](id: Any)(implicit m: Manifest[T]): T = synchronized {
    if (map.contains(id).isFalse)
      map += id -> (Mockito.mock(m.runtimeClass))
    map(id).asInstanceOf[T]
  }
}
