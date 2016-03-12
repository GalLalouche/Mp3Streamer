package search

import common.rich.RichT._

/** to allow artist's name to be factored in the song search */
object WeightedTermIndexBuilder {
  private implicit class RichMap[T, S](map: Map[T, Set[S]]) {
    def append(t: T, s: S) = map.updated(t, map(t) + s)
  }
  def buildIndexFor[T: WeightedIndexable : Indexable](songs: TraversableOnce[T]): Index[T] = songs
    .foldLeft(Map[String, Set[(T, Double)]]().withDefault(e => Set[(T, Double)]()))((map, indexable) =>
      implicitly[WeightedIndexable[T]]
        .terms(indexable)
        .map(e => e._1.toLowerCase -> e._2)
        .foldLeft(map)((map, weightedTerm) => map.append(weightedTerm._1, indexable -> weightedTerm._2)))
    .map(e => e._1 -> e._2.toVector.sortBy(-_._2))
    .mapTo(new WeightedIndex(_))

  private class WeightedIndex[T: WeightedIndexable : Indexable](weightedTerms: Map[String, Vector[(T, Double)]])
    extends Index[T](implicitly[Indexable[T]].sortBy) {
    implicit object Foobar extends Indexable[(T, Double)] {
      override def sortBy(t: (T, Double)): Product = t._2 -> implicitly[Indexable[T]].sortBy(t._1)
      override def name(t: (T, Double)): String = implicitly[Indexable[T]].name(t._1)
    }
    private def orNil(s: String): Seq[(T, Double)] = weightedTerms.getOrElse(s, Nil)
    // assume they are already sorted
    override def find(s: String): Seq[T] = orNil(s).map(_._1)
    override def findIntersection(ss: Traversable[String]): Seq[T] = {
      def aux(queries: List[String], result: Map[T, Double]): Seq[T] = queries match {
        case Nil => result.toVector.sortBy(-_._2).map(_._1)
        case (q :: qs) =>
          val newMap = orNil(q).toMap
          aux(qs, result.filterKeys(newMap.contains).map(e => e._1 -> (e._2 + newMap(e._1))))
      }
      val (h :: t) = ss.toList // so this works...
      aux(t, orNil(h).toMap)
    }
  }
}

