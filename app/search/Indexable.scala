package search

import models._

trait Indexable[T] {
  def sortBy(t: T): Product
  def name(t: T): String
  def terms(t: T): Seq[String] = name(t) split " "
}

object Indexable {
  implicit object SongIndexer extends Indexable[Song] {
    override def sortBy(s: Song): Product = (s.artistName, s.title, s.year, s.albumName, s.track)
    override def name(s: Song) = s.title
  }
  implicit object AlbumIndex extends Indexable[Album] {
    override def sortBy(a: Album): Product = (a.artistName, a.year, a.year)
    override def name(a: Album) = a.title
  }
  implicit object ArtistIndex extends Indexable[Artist] {
    override def sortBy(a: Artist): Product = Tuple1(a.name)
    override def name(a: Artist) = a.name
  }
}
