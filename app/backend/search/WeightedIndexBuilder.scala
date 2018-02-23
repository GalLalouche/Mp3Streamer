package backend.search

import backend.search.WeightedIndexable.ops._
import common.ds.Trie
import common.rich.RichT._
import common.rich.collections.RichMap._
import common.rich.collections.RichTraversableOnce._
import common.rich.func.MoreSeqInstances
import monocle.std.Tuple2Optics
import monocle.syntax.{ApplySyntax, FieldsSyntax}

import scalaz.Semigroup
import scalaz.std.SetInstances
import scalaz.syntax.{ToFunctorOps, ToSemigroupOps}

/** to allow artist's name to be factored in the song search */
private object WeightedIndexBuilder
    extends ToSemigroupOps with ToFunctorOps with SetInstances with MoreSeqInstances
        with ApplySyntax with FieldsSyntax with Tuple2Optics {
  implicit object DoubleSemi extends Semigroup[Double] {
    override def append(f1: Double, f2: => Double): Double = f1 + f2
  }

  def buildIndexFor[T: WeightedIndexable](ts: TraversableOnce[T]): Index[T] = ts
      .flatMap(e => e.terms strengthL e)
      .map(_.&|->(_2).^|->(_1).modify(_.toLowerCase))
      .aggregateMap(_._2._1, e => Set(e._1 -> e._2._2))
      .mapValues(_.toVector.sortBy(_._2))
      .mapTo(Trie.fromSeqMap)
      .mapTo(new WeightedIndex(_))

  private class WeightedIndex[T: WeightedIndexable](trie: Trie[(T, Double)]) extends Index[T] {
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      val lastQuery :: allButLast = ss.toList.reverse
      allButLast
          .map(trie.exact(_).toMap)
          ./:(trie.prefixes(lastQuery).toMap)(_ mergeIntersecting _)
          .toSeq.sortBy(-_._2)
          .map(_._1)
    }
  }
}

