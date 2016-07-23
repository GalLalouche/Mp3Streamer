package models

final case class Artist(name: String, private val _albums: Set[Album]) {
  lazy val albums = _albums.toSeq.sortBy(e => (e.year, e.title))
  def merge(a: Artist) = {
    require(a.name == name, s"Cannot merge artists with two different names (this=<$name>, that=<${a.name}>")
    Artist(name, _albums ++ a._albums)
  }
}
