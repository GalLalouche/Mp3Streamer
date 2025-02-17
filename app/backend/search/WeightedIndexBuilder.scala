package backend.search

import backend.search.WeightedIndexable.ops._

import common.rich.func.MoreIterableInstances._
import scalaz.Semigroup
import scalaz.std.set.setMonoid
import scalaz.syntax.functor.ToFunctorOps

import common.ds.Trie
import common.rich.collections.RichMap._
import common.rich.collections.RichTraversableOnce._

/**
 * Weighing allows ranking of different term sources. Like a regular index, every term points to a
 * list of documents (songs, albums, artists). In addition, every document also has a score for said
 * term. So song titles are given a higher score than the artists, e.g., If the query was "River",
 * the song "The River" would be ranked higher than songs by the band "Riverside". When searching
 * for multiple terms, the sum of all the term scores as the final score for the document is taken.
 */
private object WeightedIndexBuilder {
  private implicit object DoubleSemi extends Semigroup[Double] {
    override def append(f1: Double, f2: => Double): Double = f1 + f2
  }

  def buildIndexFor[T: WeightedIndexable](ts: Iterable[T]): Index[T] = {
    val documentsToScoredTerms: TraversableOnce[(T, (String, Double))] = ts.view
      .flatMap(e => e.terms.strengthL(e))
    val scoredDocumentByTerm: Map[String, Seq[(T, Double)]] = documentsToScoredTerms
      .aggregateMap(_._2._1, e => Set(e._1 -> e._2._2))
      .mapValues(_.toVector.sortBy(_._2))
    new WeightedIndex(Trie.fromMultiMap(scoredDocumentByTerm))
  }

  private class WeightedIndex[T: WeightedIndexable](trie: Trie[(T, Double)]) extends Index[T] {
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      val lastQuery :: allButLast = ss.toList.reverse
      allButLast
        .map(trie.exact(_).toMap)
        .foldLeft(trie.withPrefix(lastQuery).toMap)(_ mergeIntersecting _)
        .toVector
        .sortBy(-_._2)
        .map(_._1)
    }
  }
}
