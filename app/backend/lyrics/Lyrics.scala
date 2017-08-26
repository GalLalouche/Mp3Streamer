package backend.lyrics

private[lyrics] sealed trait Lyrics {
  def html: String
  def source: String
}
private[lyrics] case class HtmlLyrics(source: String, html: String) extends Lyrics {
  override def toString: String = s"(From $source)\n$html"
}
private[lyrics] case class Instrumental(source: String) extends Lyrics {
  override val html = "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b>"
  override def toString: String = s"(From $source) Instrumental"
}
