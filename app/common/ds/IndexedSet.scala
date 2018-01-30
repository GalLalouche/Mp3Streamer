package common.ds

import common.rich.func.ToMoreFoldableOps

import scalaz.Semigroup
import scalaz.std.OptionInstances
import scalaz.syntax.ToSemigroupOps

/** Sums values whose key function is equal. */
trait IndexedSet[T] extends Traversable[T] {
  def +(v: T): IndexedSet[T]
  def ++(vs: TraversableOnce[T]): IndexedSet[T] = vs.foldLeft(this)(_ + _)
  override def foreach[U](f: T => U): Unit
}

private class IndexedSetImpl[Value: Semigroup, Key](map: Map[Key, Value], index: Value => Key)
    extends IndexedSet[Value] with ToSemigroupOps with ToMoreFoldableOps with OptionInstances {
  def +(v: Value): IndexedSet[Value] = {
    val key = index(v)
    new IndexedSetImpl(map.updated(key, map.get(key).mapHeadOrElse(_ ⊹ v, v)), index)
  }
  override def foreach[U](f: Value => U) = map.values foreach f
}

object IndexedSet {
  def apply[K, V: Semigroup](index: V => K): IndexedSet[V] = new IndexedSetImpl(Map(), index)
}

