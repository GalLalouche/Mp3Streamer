package backend.albums

import enumeratum.{Enum, EnumEntry}

sealed trait AlbumType extends EnumEntry
object AlbumType extends Enum[AlbumType] {
  override val values = findValues

  case object EP extends AlbumType
  case object Album extends AlbumType
  case object Live extends AlbumType
  case object Compilation extends AlbumType
}
