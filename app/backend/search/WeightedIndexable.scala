package backend.search

import backend.recon.StringReconScorer
import backend.search.WeightedIndexable._
import models.{AlbumDir, ArtistDir, Song}
import simulacrum.typeclass

import common.rich.primitives.RichString.richString

@typeclass private trait WeightedIndexable[T] {
  protected def mainTerm(t: T): String
  protected def secondaryTerms(t: T): Iterable[String]
  private def split(s: String): Set[String] = s.toLowerCase.tokenize(StringReconScorer.Tokens).toSet
  private def weigh(s: Set[String], weight: Double) = s.map(_ -> weight)
  def terms(t: T): Iterable[(String, Double)] = {
    val primaryTerms = split(mainTerm(t))
    weigh(primaryTerms, PrimaryWeight) ++
      weigh(secondaryTerms(t).flatMap(split).toSet.filterNot(primaryTerms), SecondaryWeight)
  }
  def sortBy(t: T): Product
}

private object WeightedIndexable {
  private val PrimaryWeight = 1.0
  private val SecondaryWeight = 0.1
  implicit object SongIndexer extends WeightedIndexable[Song] {
    private def classicalMusicTerms(s: Song): Iterable[String] =
      s.composer ++ s.orchestra ++ s.conductor ++ s.opus ++ s.performanceYear.map(_.toString)
    protected override def mainTerm(t: Song) = t.title
    override def secondaryTerms(t: Song) =
      Set(t.artistName, t.albumName, t.year.toString) ++ classicalMusicTerms(t)
    override def sortBy(s: Song): Product =
      (s.artistName, s.title, s.year, s.performanceYear, s.albumName, s.trackNumber)
  }
  implicit object AlbumIndex extends WeightedIndexable[AlbumDir] {
    protected override def mainTerm(t: AlbumDir) = t.title
    protected override def secondaryTerms(t: AlbumDir) =
      t.songs.flatMap(SongIndexer.secondaryTerms).toSet
    override def sortBy(a: AlbumDir): Product = (a.artistName, a.year, a.title)
  }
  implicit object ArtistIndex extends WeightedIndexable[ArtistDir] {
    protected override def mainTerm(t: ArtistDir) = t.name
    protected override def secondaryTerms(t: ArtistDir) = Vector.empty
    override def sortBy(a: ArtistDir): Product = Tuple1(a.name)
  }
}
