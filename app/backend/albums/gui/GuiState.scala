package backend.albums.gui

import backend.albums.AlbumsModel
import javax.inject.Inject

private class GuiState(
    rowOrdering: Ordering[DataExtractor],
    orders: Seq[Order],
    private val data: Seq[AlbumsModel.ArtistAlbums],
    private val groupedDataBuilder: GroupedDataBuilder
) {
  private implicit val ordering: Ordering[DataExtractor] = rowOrdering
  def prepare: (GroupedData, Seq[DataExtractor]) = (
      groupedDataBuilder(data, orders),
      Order.filterExtractors(orders),
  )
}

private object GuiState {
  class Factory @Inject()(groupedDataBuilder: GroupedDataBuilder) {
    def apply(
        rowOrdering: Ordering[DataExtractor],
        orders: Seq[Order],
        data: Seq[AlbumsModel.ArtistAlbums],
    ): GuiState = new GuiState(rowOrdering, orders, data, groupedDataBuilder)
  }
}
