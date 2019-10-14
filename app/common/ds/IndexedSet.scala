package common.ds

import scalaz.Semigroup

import common.rich.collections.RichMap._

/** Sums values in the same equivalence relation, i.e., are considered the "same" for the purpose of a set. */
sealed trait IndexedSet[A] extends Iterable[A] {
  def +(v: A): IndexedSet[A]
  def ++(vs: TraversableOnce[A]): IndexedSet[A] = vs.foldLeft(this)(_ + _)
}

object IndexedSet {
  private class IndexedSetImpl[V: Semigroup, Key](map: Map[Key, V], index: V => Key)
      extends IndexedSet[V] {
    def +(v: V): IndexedSet[V] = new IndexedSetImpl(map.upsert(index(v), v), index)
    override def iterator = map.valuesIterator
  }

  def apply[K, V: Semigroup](index: V => K): IndexedSet[V] = new IndexedSetImpl(Map(), index)
}

