package search

import models.Song
import scala.annotation.tailrec
import models.Album
import models.Artist

trait Indexable[T] {
  private def compareBy(cs: List[T => String])(t1: T, t2: T): Boolean = cs.iterator
    .map(f => f(t1).compareTo(f(t2)))
    .find(_ != 0)
    .map(i => if (i < 0) false else true)
    .getOrElse(false)

  // items will be compared by these attributes, from first to last.
  // If they are equal on all, just return false.
  protected val compareByList: List[T => String]
  protected def name(t: T): String
  def terms(t: T): Seq[String] = name(t) split " "
  val compare: (T, T) => Boolean = compareBy(compareByList) _ // Haskell on my mind
}

object Indexable {
  implicit object SongIndexer extends Indexable[Song] {
    override protected def name(s: Song) = s.title
    override protected val compareByList = 
      List[Song => String](_.artistName, _.year.toString, _.albumName, _.title.toString)
  }
  implicit object AlbumIndex extends Indexable[Album] {
    override protected def name(a: Album) = a.title
    override protected val compareByList = 
      List[Album => String](_.artistName, _.year.toString, _.title)
  }
  implicit object ArtistIndex extends Indexable[Artist] {
    override protected def name(a: Artist) = a.name
    override protected val compareByList = 
      List[Artist => String](_.name)
  }
}