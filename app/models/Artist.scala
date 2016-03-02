package models

final class Artist(val name: String, _albums: Set[Album]) {
  override def equals(other: Any) = other match {
    case that: Artist => name == that.name
    case _            => false
  }
  lazy val albums = _albums.toSeq.sortBy(e => (e.year, e.title));
  def addAlbum(a: Album) = new Artist(name, _albums + a)
  override def hashCode() = 41 + name.hashCode
}
