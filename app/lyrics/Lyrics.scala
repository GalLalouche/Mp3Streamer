package lyrics

case class Lyrics(val html: String, val source: String) {
  override def toString: String = "(From " + source + ")\n" + html
}
