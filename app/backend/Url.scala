package backend

import org.apache.commons.validator.routines.UrlValidator

import scala.annotation.tailrec

import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

case class Url(address: String) {
  require(address.isWhitespaceOrEmpty.isFalse, "empty address")
  def host: Url = {
    val baseHost = if (address startsWith "http")
      address.split('/')(2)
    else
      address.takeWhile(_ != '/')
    Url(
      baseHost
          // E.g., https://shanipeleg1.bandcamp.com should return bandcamp.com
          .mapIf(a => a.startsWith("www.").isFalse && a.count(_ == '.') == 2)
          .to(_.dropWhile(_ != '.').tail)
    )
  }
  @tailrec final def +/(s: String): Url =
    if (s.head == '/') +/(s.tail) else Url(address + s.mapIf(address.last != '/').to('/' + _))
  def isValid: Boolean = UrlValidator.getInstance().isValid(address)
}
