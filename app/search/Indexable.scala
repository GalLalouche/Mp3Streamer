package search

import models.Song
import scala.annotation.tailrec
import models.Album
import models.Artist

trait Indexable[T] {
  protected def name(t: T): String
  def terms(t: T): Seq[String] = name(t) split " "
  def sort(ts: Seq[T]): Seq[T]
}

object Indexable {
  implicit object SongIndexer extends Indexable[Song] {
    override protected def name(s: Song) = s.title
    override def sort(ss: Seq[Song]) = ss.sortBy(s => (s.artistName, s.title, s.year, s.albumName, s.track))
  }
  implicit object AlbumIndex extends Indexable[Album] {
    override protected def name(a: Album) = a.title
    override def sort(as: Seq[Album]) = as.sortBy(a => (a.artistName, a.year, a.year))
  }
  implicit object ArtistIndex extends Indexable[Artist] {
    override protected def name(a: Artist) = a.name
    override def sort(as: Seq[Artist]) = as.sortBy(_.name)
  }
}