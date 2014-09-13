package common

import java.io.File

import scala.util.matching.Regex

class RichString($: String) {
	require($ != null)
	def removeTrailingQuotes = $.replaceAll("""^["']+|["']+$""", "")

	def trimEverythingAfterFirst(str: String): String = {
		val i = $.indexOf(str)
		$.substring(0, i + 1)
	}
	def smartSplit(str: String) = {
		val result = $.split(str).toSeq
		if ($ endsWith str)
			result :+ ""
		else
			result
	}
	// splits last item too
	def smartSplit(c: Char): Seq[String] = smartSplit(c.toString)
	def captureWith(regex: Regex) = try {
		$ match {
			case regex(result) => result
		}
	} catch {
		case e: MatchError => println(s"Could not capture $regex on string ${$}"); throw e
	}
	def dropLast(c: Char) = $.substring($.lastIndexOf(c) + 1)

	def dropAfter(str: String) = $.replaceFirst(s"($str).*", "$1")
}

object RichString {
	implicit def richString(str: String) = new RichString(str)
}