package controllers

trait UrlDecodeUtils {
  /** Can be used to decode Hebrew letters in path. */
  def apply(s: String): String
}
