package backend.albums.gui

import enumeratum.{Enum, EnumEntry}
import mains.fixer.StringFixer

import common.rich.RichT.richT

private sealed trait DataExtractor extends EnumEntry {
  def extract(e: AlbumEntry): String
  def ordering: Ordering[AlbumEntry] = Ordering.by(extract)
}
private object DataExtractor extends Enum[DataExtractor] {
  object Score extends DataExtractor {
    override def extract(e: AlbumEntry) = e.score.orDefaultString
    override def ordering: Ordering[AlbumEntry] = {
      import Order.ByScore.scoreOrdering
      Ordering.by(_.score)
    }
  }
  object Genre extends DataExtractor {
    override def extract(e: AlbumEntry) = e.genre.fold("No genre")(_.name)
  }
  object Artist extends DataExtractor {
    override def extract(e: AlbumEntry) = StringFixer(e.artist.name)
  }
  object Year extends DataExtractor {
    override def extract(e: AlbumEntry) = e.date.getYear.toString
    override def ordering: Ordering[AlbumEntry] = Ordering.by[AlbumEntry, Int](_.date.getYear).reverse
  }
  object Album extends DataExtractor {
    override def extract(e: AlbumEntry) = e.albumTitle.tryOrKeep(StringFixer.apply)
  }
  override def values = findValues
}
