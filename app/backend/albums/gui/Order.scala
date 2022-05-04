package backend.albums.gui

import backend.albums.AlbumsModel.ArtistAlbums
import backend.albums.NewAlbum
import backend.scorer.ModelScore
import enumeratum.{Enum, EnumEntry}

import common.rich.func.TuplePLenses

import common.rich.collections.RichSeq._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private sealed trait Order extends EnumEntry {
  def grouping: Order.Grouping
  def extractorToRemove: Option[DataExtractor]
}
private object Order extends Enum[Order] {
  type Grouping = Seq[AlbumEntry] => Seq[(String, Seq[AlbumEntry])]
  private def groupByArtist(label: ArtistAlbums => String): Grouping =
    groupByArtist(label, Ordering.by(label))
  private def groupByArtist(label: ArtistAlbums => String, order: Ordering[ArtistAlbums]): Grouping = _
      .groupBy(_.artist)
      .toVector
      .view
      .map {case (artist, entries) => ArtistAlbums(
        artist = artist,
        artistScore = entries.mapSingle(_.score),
        albums = entries.map(e => NewAlbum(e.albumTitle, e.date, artist, e.albumType)),
        genre = entries.mapSingle(_.genre),
      )
      }
      .sorted(order)
      .toVector
      .orderedGroupBy(label)
      .map(TuplePLenses.tuple2Second.modify(_.flatMap(AlbumEntry.from)))

  object ByGenre extends Order {
    override def extractorToRemove: Option[DataExtractor] = Some(DataExtractor.Genre)
    override def grouping: Grouping = groupByArtist(_.genre.fold("N/A")(_.name))
  }
  object ByArtist extends Order {
    override def extractorToRemove: Option[DataExtractor] = Some(DataExtractor.Artist)
    override def grouping: Grouping = groupByArtist(_.artist.name)
  }
  object ByMissingAlbums extends Order {
    override def extractorToRemove: Option[DataExtractor] = None
    override def grouping: Grouping = groupByArtist(_.albums.size.toString, Ordering.by(_.albums.size))
  }
  object ByScore extends Order {
    override def extractorToRemove: Option[DataExtractor] = Some(DataExtractor.Score)
    implicit val scoreOrdering: Ordering[Option[ModelScore]] = ModelScore.optionOrdering
    override def grouping: Grouping = groupByArtist(_.artistScore.orDefaultString, Ordering.by(_.artistScore))
  }
  object ByYear extends Order {
    override def extractorToRemove: Option[DataExtractor] = Some(DataExtractor.Year)
    override def grouping: Grouping =
      entries => entries.groupBy(_.date.getYear.toString).toVector.sortBy(_._1).reverse
  }
  override def values = findValues
  def filterExtractors(orders: Seq[Order]): Seq[DataExtractor] = {
    val toRemove = orders.flatMap(_.extractorToRemove).toSet
    DataExtractor.values.filterNot(toRemove)
  }
}
