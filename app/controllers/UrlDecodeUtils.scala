package controllers

trait UrlDecodeUtils {
  /** Can be used to decode hebrew letters in path. */
  def decode(s: String): String
}
