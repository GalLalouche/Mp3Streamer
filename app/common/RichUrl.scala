package common

import _root_.io.lemonlabs.uri.Url
import _root_.io.lemonlabs.uri.typesafe.PathPart
import org.apache.commons.validator.routines.UrlValidator

import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean

object RichUrl {
  implicit class richUrl(private val url: Url) extends AnyVal {
    def hostUrl: Url = {
      val address = url.toStringPunycode
      val baseHost =
        if (address.startsWith("http"))
          address.split('/')(2)
        else
          address.takeWhile(_ != '/')
      Url.parse(
        baseHost
          // E.g., https://shanipeleg1.bandcamp.com should return bandcamp.com
          .mapIf(a => a.startsWith("www.").isFalse && a.count(_ == '.') == 2)
          .to(_.dropWhile(_ != '.').tail),
      )
    }
    def isValid: Boolean = UrlValidator.getInstance.isValid(url.toStringPunycode)
    // Does not encode the path part
    def addPathPartRaw[P](p: P)(implicit pathPart: PathPart[P]): Url = {
      val s = pathPart.path(p)
      val address = url.toStringRaw
      if (s.head == '/') addPathPartRaw(s.tail)
      else Url.parse(address + s.mapIf(address.last != '/').to('/' + _))
    }

  }

  object Unapplied {
    def unapply(url: Url): Option[String] = Some(url.toStringPunycode)
  }
}
