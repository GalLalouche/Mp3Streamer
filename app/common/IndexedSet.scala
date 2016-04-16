package common


trait IndexedSet[Value] extends Traversable[Value] {
  // TODO replace with Monoid
  type Key
  protected val index: Function[Value, Key]
  protected val merge: Function2[Value, Value, Value]
  protected val map: Map[Key, Value]
  private def withMap(map: Map[Key, Value]): IndexedSet[Value] = IndexedSet.withMap(index, merge, map)
  def +(v: Value): IndexedSet[Value] = {
    val key = index(v)
    val newV = map.get(key).map(merge(_, v)).getOrElse(v)
    require(key == index(newV), s"Inconsistent keys for $v ($key) and new $newV (${index(newV) })")
    withMap(map + ((key, newV)))
  }
  override def foreach[U](f: Function[Value, U]) { map.values foreach f }
}

object IndexedSet {
  private def withMap[K, V](_index: Function[V, K], _merge: Function2[V, V, V], _map: Map[K, V]): IndexedSet[V] =
    new IndexedSet[V] {
      override type Key = K
      override protected val index = _index
      override protected val merge = _merge
      override protected val map = _map
    }
  def apply[K, V](index: Function[V, K], merge: Function2[V, V, V]): IndexedSet[V] = withMap(index, merge, Map())
}

