package controllers

trait Decoder {
  def apply(s: String): String
}
