package search

import models._

trait WeightedIndexable[T] {
  protected def mainName(t: T): String
  protected def secondaryName(t: T): String
  private def split(s: String, weight: Double) = s split " " map (_ -> weight)
  def terms(t: T): Seq[(String, Double)] = split(mainName(t), 1.0) ++ split(secondaryName(t), 0.1)
}

object WeightedIndexable {
  implicit object SongIndexer extends WeightedIndexable[Song] {
    override protected def mainName(t: Song): String = t.title
    override protected def secondaryName(t: Song): String = t.artistName
  }
  implicit object AlbumIndex extends WeightedIndexable[Album] {
    override protected def mainName(t: Album): String = t.title
    override protected def secondaryName(t: Album): String = t.artistName
  }
  implicit object ArtistIndex extends WeightedIndexable[Artist] {
    override protected def mainName(t: Artist): String = t.name
    override protected def secondaryName(t: Artist): String = ""
  }
}
