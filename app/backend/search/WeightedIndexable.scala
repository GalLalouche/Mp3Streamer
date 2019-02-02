package backend.search

import common.rich.func.ToMoreFoldableOps
import models._
import simulacrum.typeclass

import scalaz.std.OptionInstances

@typeclass private trait WeightedIndexable[T]
    extends ToMoreFoldableOps with OptionInstances {
  protected def mainTerm(t: T): String
  protected def secondaryTerms(t: T): Traversable[String]
  private def split(s: String, weight: Double) = s split " " map (_ -> weight)
  def terms(t: T): Seq[(String, Double)] = split(mainTerm(t), 1.0) ++ secondaryTerms(t).flatMap(split(_, 0.1))
  def sortBy(t: T): Product
}

private object WeightedIndexable {
  private def classicalMusicTerms(s: Song): Traversable[String] =
    s.composer ++ s.orchestra ++ s.conductor ++ s.opus
  implicit object SongIndexer extends WeightedIndexable[Song] {
    override protected def mainTerm(t: Song) = t.title
    override def secondaryTerms(t: Song) =
      Set(t.artistName) ++ classicalMusicTerms(t)
    override def sortBy(s: Song): Product = (s.artistName, s.title, s.year, s.albumName, s.track)
  }
  implicit object AlbumIndex extends WeightedIndexable[Album] {
    override protected def mainTerm(t: Album) = t.title
    override protected def secondaryTerms(t: Album) = t.songs.flatMap(SongIndexer.secondaryTerms).toSet
    override def sortBy(a: Album): Product = (a.artistName, a.year, a.title)
  }
  implicit object ArtistIndex extends WeightedIndexable[Artist] {
    override protected def mainTerm(t: Artist) = t.name
    override protected def secondaryTerms(t: Artist) = Vector.empty
    override def sortBy(a: Artist): Product = Tuple1(a.name)
  }
}
