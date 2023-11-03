package backend.search

import backend.search.WeightedIndexable._
import models.{Album, Artist, Song}
import simulacrum.typeclass

@typeclass private trait WeightedIndexable[T] {
  protected def mainTerm(t: T): String
  protected def secondaryTerms(t: T): Iterable[String]
  private def split(s: String) = s.toLowerCase.split(" ")
  private def weigh(s: Set[String], weight: Double) = s.map(_ -> weight)
  def terms(t: T): Iterable[(String, Double)] = {
    val primaryTerms = split(mainTerm(t)).toSet
    weigh(primaryTerms, PrimaryWeight) ++
      weigh(secondaryTerms(t).flatMap(split).toSet.filterNot(primaryTerms), SecondaryWeight)
  }
  def sortBy(t: T): Product
}

private object WeightedIndexable {
  private val PrimaryWeight = 1.0
  private val SecondaryWeight = 0.1
  implicit object SongIndexer extends WeightedIndexable[Song] {
    private def classicalMusicTerms(s: Song): Traversable[String] =
      s.composer ++ s.orchestra ++ s.conductor ++ s.opus ++ s.performanceYear.map(_.toString)
    protected override def mainTerm(t: Song) = t.title
    override def secondaryTerms(t: Song) =
      Set(t.artistName, t.albumName, t.year.toString) ++ classicalMusicTerms(t)
    override def sortBy(s: Song): Product =
      (s.artistName, s.title, s.year, s.performanceYear, s.albumName, s.track)
  }
  implicit object AlbumIndex extends WeightedIndexable[Album] {
    protected override def mainTerm(t: Album) = t.title
    protected override def secondaryTerms(t: Album) =
      t.songs.flatMap(SongIndexer.secondaryTerms).toSet
    override def sortBy(a: Album): Product = (a.artistName, a.year, a.title)
  }
  implicit object ArtistIndex extends WeightedIndexable[Artist] {
    protected override def mainTerm(t: Artist) = t.name
    protected override def secondaryTerms(t: Artist) = Vector.empty
    override def sortBy(a: Artist): Product = Tuple1(a.name)
  }
}
