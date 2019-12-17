package backend.external

import enumeratum.{Enum, EnumEntry}

private sealed trait LinkMark extends EnumEntry

private object LinkMark extends Enum[LinkMark] {
  override val values = findValues
  case object None extends LinkMark
  case object New extends LinkMark
  case object Missing extends LinkMark
}
