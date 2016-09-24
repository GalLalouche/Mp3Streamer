package search

import common.Collectable._
import common.ds.RichMap._
import common.ds.Trie
import common.rich.RichT._

import scalaz.Semigroup

/** to allow artist's name to be factored in the song search */
private object WeightedIndexBuilder {
  def buildIndexFor[T: WeightedIndexable : Indexable](songs: TraversableOnce[T]): Index[T] = songs
    ./:(Map[String, Set[(T, Double)]]())((map, indexable) => implicitly[WeightedIndexable[T]]
      .terms(indexable)
      .map(e => e._1.toLowerCase -> e._2)
      ./:(map)((map, weightedTerm) => map.append(weightedTerm._1, indexable -> weightedTerm._2)))
    .map(e => e._1 -> e._2.toSeq.sortBy(-_._2)) |> Trie.fromSeqMap |> (WeightedIndex(_))

  private case class WeightedIndex[T: WeightedIndexable : Indexable](trie: Trie[(T, Double)]) extends Index[T] {
    implicit object DoubleSemi extends Semigroup[Double] { // seriously?
      override def append(f1: Double, f2: => Double): Double = f1 + f2
    }
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      val lastQuery :: allButLast = ss.toList.reverse
      allButLast
        .map(trie.exact(_).toMap)
        ./:(trie.prefixes(lastQuery).toMap)(_ merge _)
        .toSeq.sortBy(-_._2).map(_._1)
    }
  }
}

