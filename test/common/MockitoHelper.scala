package common

import common.rich.CacheMap
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import scala.collection.mutable

trait MockitoHelper {
  private val map = new mutable.HashMap[Any, Any]()
  def mockWithId[T](id: Any)(implicit m: Manifest[T]): T = synchronized {
    if (map.contains(id) == false)
      map += id -> (Mockito mock m.runtimeClass)
    map(id).asInstanceOf[T]
  }
}
