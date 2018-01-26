package backend

import common.rich.primitives.RichBoolean._
import java.net.URL

case class Url(address: String) {
  def toURL: URL = new URL(address)

  require(address.matches("\\s*").isFalse, "empty address")
  def host = Url(
    if (address startsWith "http")
      address split '/' apply 2
    else
      address takeWhile (_ != '/'))
  def +/(s: String): Url =
    if (address.last == '/' || s.head == '/')
      Url(address + (if (address.last == s.head) s.drop(1) else s))
    else
      Url(s"$address/$s")
}
