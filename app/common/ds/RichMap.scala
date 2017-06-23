package common.ds

import scalaz.Semigroup

// TODO move to common project
// TODO a lot of this can be probably be replaced with lenses
object RichMap {
  type Merger[V] = (V, V) => V
  implicit class richMap[K, V]($: Map[K, V]) {
    def updateWith(k: K, v: V, merge: Merger[V]): Map[K, V] = $.updated(k, $.get(k).map(merge(v, _)).getOrElse(v))
    def updateWith(kv: Seq[(K, V)], merge: Merger[V]): Map[K, V] = kv./:($) {
      case (map, (k, v)) => richMap(map).updateWith(k, v, merge)
    }
    def merge(other: Map[K, V], merge: Merger[V]): Map[K, V] = updateWith(other.toSeq, merge)
    def modified(k: K, f: V => V): Map[K, V] = $.updated(k, f($(k)))
  }
  implicit class RichMapCollectable[K, V, C]($: Map[K, C])(implicit c: SemiCollectable[V, C]) {
    def append(k: K, v: V): Map[K, C] = $.updated(k, $.get(k).map(c.+(_, v)).getOrElse(c pure v))
  }
  implicit class RichMapSemi[K, V: Semigroup]($: Map[K, V]) {
    private val append: Merger[V] = implicitly[Semigroup[V]].append(_, _)
    def merge(other: Map[K, V]): Map[K, V] = richMap($).merge(other, append)
    def updateAppend(k: K, v: V): Map[K, V] = richMap($).updateWith(k, v, append)
    def updateAppend(kv: Seq[(K, V)]): Map[K, V] = richMap($).updateWith(kv, append)
  }
}
