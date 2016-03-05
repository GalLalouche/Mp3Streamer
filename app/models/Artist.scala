package models

final case class Artist(val name: String, private val _albums: Set[Album]) {
  lazy val albums = _albums.toSeq.sortBy(e => (e.year, e.title));
  def merge(a: Artist) = {
    require(a.name == name, s"Cannot merge artists with two different names (this=<$name>, that=<${a.name}>")
    new Artist(name, _albums ++ a._albums)
  }
}
