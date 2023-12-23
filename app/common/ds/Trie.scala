package common.ds

import scala.annotation.tailrec

import common.rich.RichTuple._

sealed trait Trie[+A] {
  def +[B >: A](key: String, v: B): Trie[B]
  def +[B >: A](e: (String, B)): Trie[B]
  /** Returns all values whose key is prefixed by the input key. */
  def withPrefix(key: String): Iterable[A]
  /** Returns all values with the exact key. */
  def exact(key: String): Iterable[A]
  /** The number of values in this Trie. */
  def size: Int
}

object Trie {
  private case class TrieImpl[+A](map: Map[Char, TrieImpl[A]], values: Vector[A]) extends Trie[A] {
    private def getOrEmpty(c: Char): TrieImpl[A] = map.getOrElse(c, Nil)
    override def +[B >: A](key: String, v: B): TrieImpl[B] =
      if (key.isEmpty) copy(values = values :+ v)
      else copy(map = map + (key.head -> (getOrEmpty(key.head) + (key.tail -> v))))
    @inline def +[B >: A](e: (String, B)): TrieImpl[B] = e.reduce(this.+)
    private def allValues: Iterable[A] = values ++ map.values.flatMap(_.allValues)
    override lazy val size: Int = values.size + map.valuesIterator.map(_.size).sum
    @tailrec
    private def aux[B >: A](key: String, onEmptyKey: TrieImpl[B] => Iterable[B]): Iterable[B] =
      if (key.isEmpty) onEmptyKey(this) else getOrEmpty(key.head).aux(key.tail, onEmptyKey)
    override def withPrefix(key: String): Iterable[A] = aux(key, onEmptyKey = _.allValues)
    override def exact(key: String): Iterable[A] = aux(key, onEmptyKey = _.values)
  }

  private val Nil: TrieImpl[Nothing] = TrieImpl(Map.empty, Vector.empty)
  def empty[A]: Trie[A] = Nil

  def fromMap[A](map: Map[String, A]): Trie[A] = map.foldLeft(empty[A])(_ + _)
  def fromMultiMap[A](map: Map[String, Iterable[A]]): Trie[A] = map.foldLeft(empty[A]) {
    case (trie, (key, value)) => value.foldLeft(trie)(_.+(key, _))
  }
}
