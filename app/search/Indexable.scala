package search

import models._

trait Indexable[T] {
  def sortBy(t: T): Product
  protected def name(t: T): String
  def terms(t: T): Seq[String] = name(t) split " "
}

object Indexable {
  implicit object SongIndexer extends Indexable[Song] {
    override def sortBy(s: Song): Product = (s.artistName, s.title, s.year, s.albumName, s.track)
    override protected def name(s: Song) = s.title
  }
  implicit object AlbumIndex extends Indexable[Album] {
    override def sortBy(a: Album): Product = (a.artistName, a.year, a.year)
    override protected def name(a: Album) = a.title
  }
  implicit object ArtistIndex extends Indexable[Artist] {
    override def sortBy(a: Artist): Product = Tuple1(a.name)
    override protected def name(a: Artist) = a.name
  }
}
