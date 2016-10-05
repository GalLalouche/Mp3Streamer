package common

import scalaz.Semigroup


abstract class IndexedSet[Value : Semigroup] extends Traversable[Value] {
  type Key
  protected val index: Value => Key
  protected val map: Map[Key, Value] // can't be a constructor object since it depends on the type
  private def withMap(map: Map[Key, Value]): IndexedSet[Value] = IndexedSet.withMap(index, map)
  def +(v: Value): IndexedSet[Value] = {
    val key = index(v)
    val newV = map.get(key).map(implicitly[Semigroup[Value]].append(_, v)).getOrElse(v)
    require(key == index(newV), s"Inconsistent keys for $v ($key) and new $newV (${index(newV) })")
    withMap(map + ((key, newV)))
  }
  def ++(vs: TraversableOnce[Value]): IndexedSet[Value] = vs.foldLeft(this)(_ + _)
  override def foreach[U](f: Value => U) { map.values foreach f }
}

object IndexedSet {
  private def withMap[K, V : Semigroup](_index: V => K, _map: Map[K, V]): IndexedSet[V] =
    new IndexedSet[V] {
      override type Key = K
      override protected val index = _index
      override protected val map = _map
    }
  def apply[K, V : Semigroup](index: V => K): IndexedSet[V] = withMap(index, Map())
}

