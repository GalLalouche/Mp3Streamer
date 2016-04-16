package decoders

import java.io.File

trait CueSplitter {
	def split(cuePath: String) { split(new File(cuePath)) }
	def split(cueFile: File)
}
