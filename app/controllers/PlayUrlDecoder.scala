package controllers

import java.net.URLDecoder

import common.rich.RichT._
import common.rich.primitives.RichString._

private object PlayUrlDecoder extends UrlDecodeUtils {
  private val Encoding = "UTF-8"
  private val EncodedPlus = "%2B"
  private val SpaceEncoding = "%20"

  override def apply(s: String): String = {
    // Play converts %2B to '+' (see above), which is in turned decoded as ' '. To fix this bullshit, '+' is
    // manually converted back to "%2B" if there are "%20" tokens, which (presumably) means that '+' isn't
    // used for spaces.
    val fixedPath = s.mapIf(_ contains SpaceEncoding).to(_.simpleReplace("+", EncodedPlus))
    URLDecoder.decode(fixedPath, Encoding)
  }
}
