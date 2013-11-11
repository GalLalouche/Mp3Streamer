package common.io

import java.io.InputStream
import scalax.io.Resource

class RichStream(val is: InputStream) {
	require(is != null)

	def readAll: String = scala.io.Source.fromInputStream(is).getLines().mkString("\n");

	def getBytes: Array[Byte] = Resource.fromInputStream(is).byteArray
}

object RichStream {
	implicit def richStream(is: InputStream) = new RichStream(is)
}