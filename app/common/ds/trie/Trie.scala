package common.ds.trie

trait Trie[+A] {
  /** Returns all values whose key is prefixed by the input key. */
  def withPrefix(key: String): Iterable[A]
  /** Returns all values with the exact key. */
  def exact(key: String): Iterable[A]
  /** The number of values in this Trie. */
  def size: Int
}
