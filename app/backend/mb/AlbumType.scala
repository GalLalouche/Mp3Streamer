package backend.mb

import enumeratum.{Enum, EnumEntry}

// TODO revisit visibility - widened for server test access
sealed trait AlbumType extends EnumEntry
object AlbumType extends Enum[AlbumType] {
  override val values = findValues

  case object Album extends AlbumType
  case object EP extends AlbumType
  case object Live extends AlbumType
  case object Compilation extends AlbumType
  case object Single extends AlbumType
}
