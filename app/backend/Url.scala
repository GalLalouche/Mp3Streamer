package backend

import java.net.URL

import org.apache.commons.validator.routines.UrlValidator

import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

case class Url(address: String) {
  def toURL: URL = new URL(address)

  require(address.isWhitespaceOrEmpty.isFalse, "empty address")
  def host = Url(
    if (address startsWith "http")
      address split '/' apply 2
    else
      address takeWhile (_ != '/'))
  def +/(s: String): Url = Url(
    if (address.last == '/' || s.head == '/')
      address + (if (address.last == s.head) s.drop(1) else s)
    else
      s"$address/$s"
  )
  def isValid: Boolean = UrlValidator.getInstance().isValid(address)
}
