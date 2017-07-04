package search

import common.ds.Trie
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import monocle.std.Tuple2Optics
import monocle.syntax.{ApplySyntax, FieldsSyntax}

import scalaz.Semigroup
import scalaz.std.SetInstances
import scalaz.syntax.ToSemigroupOps

/** to allow artist's name to be factored in the song search */
private object WeightedIndexBuilder
    extends ToSemigroupOps with SetInstances
        with ApplySyntax with FieldsSyntax with Tuple2Optics {
  import WeightedIndexable.ops._
  implicit object DoubleSemi extends Semigroup[Double] {
    override def append(f1: Double, f2: => Double): Double = f1 + f2
  }

  def buildIndexFor[T: WeightedIndexable](ts: TraversableOnce[T]): Index[T] = ts
      .flatMap(e => e.terms.map(e -> _))
      .map(_.&|->(_2).^|->(_1).modify(_.toLowerCase))
      .aggregateMap(_._2._1, e => Set(e._1 -> e._2._2))
      .mapValues(_.toVector.sortBy(_._2))
      .mapTo(Trie.fromSeqMap)
      .mapTo(new WeightedIndex(_))

  private class WeightedIndex[T: WeightedIndexable](trie: Trie[(T, Double)]) extends Index[T] {
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

