package backend.mb

import enumeratum.{Enum, EnumEntry}

private[backend] sealed trait AlbumType extends EnumEntry
private[backend] object AlbumType extends Enum[AlbumType] {
  override val values = findValues

  case object EP extends AlbumType
  case object Album extends AlbumType
  case object Live extends AlbumType
  case object Compilation extends AlbumType
  case object Single extends AlbumType
}
