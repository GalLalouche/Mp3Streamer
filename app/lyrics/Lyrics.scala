package lyrics

sealed abstract class Lyrics {
  val html: String
  val source: String
}
case class HtmlLyrics(source: String, html: String) extends Lyrics {
  override def toString: String = "(From " + source + ")\n" + html
}
case class Instrumental(source: String) extends Lyrics {
  override val html = "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b>"
  override def toString: String = "(From " + source + ")\n" + "Instrumental"
}
