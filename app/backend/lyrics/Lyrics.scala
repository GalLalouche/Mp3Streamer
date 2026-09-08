package backend.lyrics

private sealed trait Lyrics {
  def html: String
  def source: String
  def url: LyricsUrl
}
private case class HtmlLyrics(source: String, html: String, url: LyricsUrl) extends Lyrics {
  override def toString: String = s"(From $source, $url)\n$html"
}
private case class Instrumental(source: String, url: LyricsUrl) extends Lyrics {
  override val html = "<img src='assets/images/TrebleClef.svg' width='60' /><b>Instrumental</b>"
  override def toString: String = s"(From $source, $url) Instrumental"
}
