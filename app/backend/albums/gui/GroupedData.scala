package backend.albums.gui

private sealed trait GroupedData
// Recursively nested, each time by a different grouping, e.g., by genre, then by year, then by score.
private case class Grouped(groups: Seq[(String, GroupedData)]) extends GroupedData
// Lowest level isn't grouped, just a list of albums
private case class Rows(entries: Seq[AlbumEntry]) extends GroupedData
