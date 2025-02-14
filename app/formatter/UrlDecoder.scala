package formatter

trait UrlDecoder {
  def apply(s: String): String
}
