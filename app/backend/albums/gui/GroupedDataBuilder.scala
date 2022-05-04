package backend.albums.gui

import backend.albums.AlbumsModel.ArtistAlbums
import backend.recon.Artist
import javax.inject.Inject
import mains.fixer.StringFixer

import scalaz.std.function.function1Instance
import scalaz.syntax.arrow.ToArrowOps
import common.rich.func.TuplePLenses
import monocle.Monocle.toApplyLensOps

import common.rich.RichT.richT

private class GroupedDataBuilder @Inject()(stringFixer: StringFixer) {
  private def maybeFixString: String => String = _.tryOrKeep(stringFixer.apply)
  private def normalizeAlbum(albumEntry: AlbumEntry): AlbumEntry = albumEntry
      .&|->(AlbumEntry.artist).^|->(Artist.name).modify(maybeFixString)
      .&|->(AlbumEntry.albumTitle).modify(maybeFixString)
  private def normalize: GroupedData => GroupedData = {
    case Rows(entries) => Rows(entries.map(normalizeAlbum))
    case Grouped(groups) => Grouped(groups.map(maybeFixString *** normalize))
  }
  def apply(data: Seq[ArtistAlbums], orders: Seq[Order])(implicit o: Ordering[DataExtractor]): GroupedData = {
    val sorter = Order.filterExtractors(orders).min

    def go(data: Seq[AlbumEntry], orders: List[Order]): GroupedData = orders match {
      case Nil => Rows(data.sorted(sorter.ordering))
      case o :: os => data
          .|>(o.grouping)
          .map(TuplePLenses.tuple2Second.modify(go(_, os)))
          .|>(Grouped)
    }
    go(data.flatMap(AlbumEntry.from), orders.toList) |> normalize
  }
}
