//package search
//
//import models.Song
//
///** builds the index for exact matches */
//object SimpleIndexBuilder extends IndexBuilder {
//  def buildIndexFor[T <: Indexable](songs: TraversableOnce[Song], f: Song => T): Index[T] = {
//    def groupBy(ts: TraversableOnce[Song], f: Song => T): Map[String, Seq[T]] = {
//      ts.map(f).map(e => e -> e.terms)
//      ???
//    }
//    ???
////      songs.map(f).toSet.map(e: T => e.terms)
////      ts.foldLeft(Map[S, List[T]]().withDefault(e => Nil)) {
////        case (map, t) =>
////          val key = f(t)
////          map.updated(key, t :: map(key))
////      }
////    }
////    new Index[T](
////      groupBy(songs, f)
////        .map(e => e._1 -> e._2.toVector)
////        .toMap)
//  }
//}