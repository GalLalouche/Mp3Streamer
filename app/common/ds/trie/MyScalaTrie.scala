package common.ds.trie

import scala.annotation.tailrec

object MyScalaTrie extends TrieBuilder {
  // This is a case class to get free copy.
  private case class TrieImpl[+A](map: Map[Char, TrieImpl[A]], values: Vector[A])
      extends PersistentTrie[A] {
    private def getOrEmpty(c: Char): TrieImpl[A] = map.getOrElse(c, Empty)
    override def +[B >: A](e: (String, B)): TrieImpl[B] = add(e._1, 0, e._2)
    private def add[B >: A](s: String, offset: Int, b: B): TrieImpl[B] =
      if (offset == s.length) copy(values = values :+ b)
      else copy(map = map + (s(offset) -> getOrEmpty(s(offset)).add(s, offset + 1, b)))
    private def allValues: Iterable[A] = values ++ map.values.flatMap(_.allValues)
    override lazy val size: Int = values.size + map.valuesIterator.map(_.size).sum
    @tailrec
    private def aux[B >: A](key: String, onEmptyKey: TrieImpl[B] => Iterable[B]): Iterable[B] =
      if (key.isEmpty) onEmptyKey(this) else getOrEmpty(key.head).aux(key.tail, onEmptyKey)
    override def withPrefix(key: String): Iterable[A] = aux(key, onEmptyKey = _.allValues)
    override def exact(key: String): Iterable[A] = aux(key, onEmptyKey = _.values)
  }

  private val Empty: TrieImpl[Nothing] = TrieImpl(Map.empty, Vector.empty)

  override def empty[A]: PersistentTrie[A] = Empty
  override def fromMap[A](map: Map[String, A]): PersistentTrie[A] = map.foldLeft(empty[A])(_ + _)
  override def fromMultiMap[A](map: Map[String, Iterable[A]]): PersistentTrie[A] =
    map.foldLeft(empty[A]) { case (trie, (key, value)) =>
      value.foldLeft(trie)(_.+(key, _))
    }
}
