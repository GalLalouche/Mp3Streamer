package common.ds

import scalaz.Semigroup
import RichMap._

/** Sums values whose key function is equal. */
trait IndexedSet[T] extends Traversable[T] {
  def +(v: T): IndexedSet[T]
  def ++(vs: TraversableOnce[T]): IndexedSet[T]
  override def foreach[U](f: T => U)
}

private class IndexedSetImpl[Value: Semigroup, Key](map: Map[Key, Value], index: Value => Key) extends IndexedSet[Value] {
  def +(v: Value): IndexedSet[Value] =
    new IndexedSetImpl(map.updateWith(index(v), v, implicitly[Semigroup[Value]].append(_, _)), index)
  def ++(vs: TraversableOnce[Value]): IndexedSet[Value] = vs.foldLeft(this: IndexedSet[Value])(_ + _)
  override def foreach[U](f: Value => U) {map.values foreach f}
}

object IndexedSet {
  def apply[K, V: Semigroup](index: V => K): IndexedSet[V] = new IndexedSetImpl(Map(), index)
}

