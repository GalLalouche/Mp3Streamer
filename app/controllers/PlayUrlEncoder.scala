package controllers

import java.net.URLEncoder

import common.io.PathRef
import common.rich.RichT._
import common.rich.primitives.RichString._

private object PlayUrlEncoder extends UrlEncoderUtils {
  private val Encoding = "UTF-8"
  private val SpaceEncoding = "%20"

  override def apply(f: PathRef): String =
    // For reasons which are beyond me, Play, being the piece of shit that it is, will try to decode %2B as
    // '+' (despite the documentation claiming that it shouldn't), which combined with the encoding of ' '
    // to '+' messes stuff up. The easiest way to solve this is by manually encoding ' ' to "%20" when a '+'
    // is present in the path.
    URLEncoder
      .encode(f.path, Encoding)
      .mapIf(f.path.contains("+"))
      .to(_.simpleReplace("+", SpaceEncoding))
}
