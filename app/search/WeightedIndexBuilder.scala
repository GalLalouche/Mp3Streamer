package search

import common.ds.Collectable._
import common.ds.RichMap._
import common.ds.Trie
import common.rich.RichT._

import scalaz.Semigroup
import scalaz.syntax.ToSemigroupOps

/** to allow artist's name to be factored in the song search */
private object WeightedIndexBuilder
    extends ToSemigroupOps {
  import WeightedIndexable.ops._
  // TODO replace with some kind of map builder from RichTraversableOnce
  def buildIndexFor[T: WeightedIndexable : Indexable](ts: TraversableOnce[T]): Index[T] = ts
      ./:(Map[String, Set[(T, Double)]]()) {(map, indexable) =>
        indexable.terms
            .map(e => e.copy(_1 = e._1.toLowerCase)) // TODO lenses
            ./:(map)((map, weightedTerm) => map.append(weightedTerm._1, indexable -> weightedTerm._2))
      }.map(e => e._1 -> e._2.toSeq.sortBy(-_._2)) |> Trie.fromSeqMap |> (WeightedIndex(_))

  implicit object DoubleSemi extends Semigroup[Double] {
    override def append(f1: Double, f2: => Double): Double = f1 + f2
  }

  private case class WeightedIndex[T: WeightedIndexable : Indexable](trie: Trie[(T, Double)]) extends Index[T] {

    private def mergeIntersectingKeys[K, V: Semigroup](m1: Map[K, V], m2: Map[K, V]): Map[K, V] =
      m1.filterKeys(m2.contains).map(e => (e._1, e._2 |+| m2(e._1)))
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      val lastQuery :: allButLast = ss.toList.reverse
      allButLast
          .map(trie.exact(_).toMap)
          ./:(trie.prefixes(lastQuery).toMap)(mergeIntersectingKeys(_, _))
          .toSeq.sortBy(-_._2)
          .map(_._1)
    }
  }
}

