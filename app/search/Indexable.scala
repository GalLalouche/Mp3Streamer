package search

import models.Song
import scala.annotation.tailrec

trait Indexable[T] {
  private def compareBy(cs: List[T => String])(t1: T, t2: T): Boolean = cs.iterator
    .map(f => f(t1).compareTo(f(t2)))
    .find(_ != 0)
    .map(i => if (i < 0) false else true)
    .getOrElse(false)
    
  // items will be compared by these attributes, from first to last. If they on all, just return false
  protected val compareByList: List[T => String]
  def terms(t: T): Seq[String]
  def extractFromSong(s: Song): T
  val compare: (T, T) => Boolean = compareBy(compareByList) _ // Haskell on my mind
}

object Indexable {
  implicit object SongIndexer extends Indexable[Song] {
    override def terms(s: Song) = s.title split " "
    override protected val compareByList = List[Song => String](_.artist, _.year.toString, _.albumName, _.title.toString)
    def extractFromSong(s: Song) = s
  }
}