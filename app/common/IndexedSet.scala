package common


trait IndexedSet[Value] extends Traversable[Value] {
  // TODO replace with Monoid
  type Key
  protected val index: Value => Key
  protected val merge: (Value, Value) => Value
  protected val map: Map[Key, Value]
  private def withMap(map: Map[Key, Value]): IndexedSet[Value] = IndexedSet.withMap(index, merge, map)
  def +(v: Value): IndexedSet[Value] = {
    val key = index(v)
    val newV = map.get(key).map(merge(_, v)).getOrElse(v)
    require(key == index(newV), s"Inconsistent keys for $v ($key) and new $newV (${index(newV) })")
    withMap(map + ((key, newV)))
  }
  override def foreach[U](f: Value => U) { map.values foreach f }
}

object IndexedSet {
  private def withMap[K, V](_index: V => K, _merge: (V, V) => V, _map: Map[K, V]): IndexedSet[V] =
    new IndexedSet[V] {
      override type Key = K
      override protected val index = _index
      override protected val merge = _merge
      override protected val map = _map
    }
  def apply[K, V](index: V => K, merge: (V, V) => V): IndexedSet[V] = withMap(index, merge, Map())
}

