package common

import common.rich.primitives.RichBoolean._
import org.mockito.Mockito

import scala.collection.mutable

// TODO Change from trait to composition
trait MockitoHelper {
  private val map = new mutable.HashMap[Any, Any]()
  def mockWithId[T](id: Any)(implicit m: Manifest[T]): T = synchronized {
    if (map.contains(id).isFalse)
      map += id -> (Mockito mock m.runtimeClass)
    map(id).asInstanceOf[T]
  }
}
