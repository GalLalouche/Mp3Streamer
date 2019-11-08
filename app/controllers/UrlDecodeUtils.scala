package controllers

trait UrlDecodeUtils {
  /** Can be used to decode Hebrew letters in path. */
  def decode(s: String): String
}
