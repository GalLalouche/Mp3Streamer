package common.ds
import common.SemiCollectable

import scalaz.Semigroup

object RichMap {
  implicit class RichMap[K, V]($: Map[K, V]) {
    def updateWith(k: K, v: V, merge: (V, V) => V): Map[K, V] = $.updated(k, $.get(k).map(merge(v, _)).getOrElse(v))
  }
  implicit class RichMapCollectable[K, V, C]($: Map[K, C])(implicit c: SemiCollectable[V, C]) {
    def append(k: K, v: V): Map[K, C] = $.updated(k, $.get(k).map(c.+(_, v)).getOrElse(c pure v))
  }
  implicit class RichMapSemi[K, V: Semigroup]($: Map[K, V]) {
    def merge(other: Map[K, V]): Map[K, V] =
      $.filterKeys(other.contains).map(e => e._1 -> implicitly[Semigroup[V]].append(e._2, other(e._1)))
  }
}