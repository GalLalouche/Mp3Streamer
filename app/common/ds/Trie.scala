package common.ds
import scala.annotation.tailrec

class Trie[+T] private(map: Map[Char, Trie[T]], private val values: List[T]) {
  private def orDefault(c: Char): Trie[T] = map.getOrElse(c, EmptyTrie)
  def this() = this(Map(), Nil)
  final def +[S >: T](key: String, v: S): Trie[S] =
    if (key.isEmpty)
      new Trie(map, v :: values)
    else
      new Trie(map + ((key.head ,orDefault(key.head).+[S](key.tail, v))), values)
  final def +[S >: T](e: (String, S)): Trie[S] = this.+(e._1, e._2)
  private def allValues: Seq[T] = values ++ map.values.flatMap(_.allValues)
  @tailrec
  final def prefixes(key: String): Seq[T] =
    if (key.isEmpty)
      allValues
    else
      orDefault(key.head).prefixes(key.tail)
  @tailrec
  final def exact(key: String): Seq[T] =
    if (key.isEmpty)
      values
    else
      orDefault(key.head).exact(key.tail)
  def size: Int = values.size + map.values.map(_.size).sum
}
object Trie {
  def fromMap[T](map: Map[String, T]): Trie[T] = map./:(new Trie[T])(_ + _)
  def fromSeqMap[T](map: Map[String, Seq[T]]): Trie[T] = map./:(new Trie[T]) {
    case (trie, (key, value)) => value./:(trie)(_.+(key, _))
  }
}
private object EmptyTrie extends Trie[Nothing]
