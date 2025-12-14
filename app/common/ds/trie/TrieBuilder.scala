package common.ds.trie

trait TrieBuilder {
  def empty[A]: Trie[A]
  def fromMap[A](map: Map[String, A]): Trie[A]
  def fromMultiMap[A](map: Map[String, Iterable[A]]): Trie[A]
}
