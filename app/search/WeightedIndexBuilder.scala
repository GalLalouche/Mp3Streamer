package search

import common.ds.Trie
import common.rich.RichT._

import scala.annotation.tailrec

/** to allow artist's name to be factored in the song search */
object WeightedIndexBuilder {
  private implicit class RichMap[T, S](map: Map[T, Set[S]]) {
    def append(t: T, s: S) = map.updated(t, map(t) + s)
  }
  def buildIndexFor[T: WeightedIndexable : Indexable](songs: TraversableOnce[T]): Index[T] = songs
    .foldLeft(Map[String, Set[(T, Double)]]().withDefault(e => Set[(T, Double)]()))((map, indexable) =>
      implicitly[WeightedIndexable[T]]
        .terms(indexable)
        .map(e => e._1.toLowerCase -> e._2)
        ./:(map)((map, weightedTerm) => map.append(weightedTerm._1, indexable -> weightedTerm._2)))
    .map(e => e._1 -> e._2.toSeq.sortBy(-_._2)) |> Trie.fromSeqMap |> (new WeightedIndex(_))

  private case class WeightedIndex[T: WeightedIndexable : Indexable](trie: Trie[(T, Double)]) extends Index[T] {
    implicit object TupleIndexable extends Indexable[(T, Double)] {
      override def sortBy(t: (T, Double)): Product = t._2 -> implicitly[Indexable[T]].sortBy(t._1)
      override def name(t: (T, Double)): String = implicitly[Indexable[T]].name(t._1)
    }
    // assume they are already sorted
    override def find(s: String): Seq[T] = trie prefixes s map (_._1)
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      if (ss.size == 1)
        return find(ss.head)
      @tailrec
      def aux(queries: List[String], result: Map[T, Double]): Seq[T] = queries match {
        case Nil => result.toVector.sortBy(-_._2).map(_._1)
        case (q :: qs) =>
          val newMap = (if (qs.nonEmpty) trie exact q else trie prefixes q).toMap
          aux(qs, result.filterKeys(newMap.contains).map(e => e._1 -> (e._2 + newMap(e._1))))
      }
      val (h :: t) = ss.toList // so this works...
      aux(t, trie.exact(h).toMap)
    }
    override def sortBy(t: T): Product = implicitly[Indexable[T]].sortBy(t)
  }
}

