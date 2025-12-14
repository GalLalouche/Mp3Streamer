package common.ds.trie

trait PersistentTrie[+A] extends Trie[A] {
  def +[B >: A](e: (String, B)): PersistentTrie[B]
}
