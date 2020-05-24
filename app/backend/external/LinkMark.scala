package backend.external

import enumeratum.{Enum, EnumEntry}

private sealed trait LinkMark extends EnumEntry

private object LinkMark extends Enum[LinkMark] {
  override val values = findValues
  case object None extends LinkMark
  case object New extends LinkMark
  case object Missing extends LinkMark
  case class Text(s: String) extends LinkMark
  object Text {
    def read(s: String): Text = {
      require(s.startsWith("Text("), s)
      Text(s.substring(5, s.length - 1))
    }
  }
}
