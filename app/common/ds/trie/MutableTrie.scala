package common.ds.trie

import scala.annotation.tailrec
import scala.collection.mutable

import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps

import common.ds.trie.MutableTrie.Node

class MutableTrie[A] extends Trie[A] {
  private var _size: Int = 0
  override def size: Int = _size
  private val node = new Node[A]()
  def addOne(s: String, elem: A): Unit = {
    node.go(s, 0, Vector(elem))
    _size += 1
  }
  def addAll(s: String, elems: Iterable[A]): Unit = {
    node.go(s, 0, elems)
    _size += 1
  }
  override def withPrefix(key: String): Iterable[A] = node.withPrefix(key, 0)
  override def exact(key: String): Iterable[A] = node.exact(key, 0)
}

object MutableTrie {
  def apply[A](): MutableTrie[A] = new MutableTrie[A]()
  def fromMultiMap[A](map: Map[String, Iterable[A]]): MutableTrie[A] = {
    val $ = MutableTrie[A]()
    map.foreach(Function.tupled($.addAll))
    $
  }
  private final class Node[A] {
    private val map = mutable.Map[Char, Node[A]]()
    private val values = mutable.ArrayBuffer[A]()

    // TODO implement this using CharBuffer?
    @tailrec def go(s: String, offset: Int, elem: Iterable[A]): Unit =
      if (offset == s.length)
        values ++= elem
      else
        map.getOrElseUpdate(s(offset), new Node[A]()).go(s, offset + 1, elem)
    @tailrec def exact(key: String, offset: Int): Iterable[A] =
      if (offset == key.length)
        values
      else
        map.get(key(offset)) match {
          case Some(child) => child.exact(key, offset + 1)
          case None => Iterable.empty
        }
    def withPrefix(key: String, offset: Int): Iterable[A] =
      if (offset == key.length)
        allValues
      else
        map.get(key(offset)).mapHeadOrElse(_.withPrefix(key, offset + 1), Iterable.empty)
    private def allValues: Iterable[A] = values.view ++ map.values.flatMap(_.allValues)
  }
}
