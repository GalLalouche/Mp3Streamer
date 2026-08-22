package songs.selector.filter

private[selector] trait MultiStageFilterFactory {
  def next(): MultiStageFilter
}
