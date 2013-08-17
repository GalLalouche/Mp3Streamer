package models

import decoders.Mp3Decoder
import java.io.File
import common.path.Path._

trait Decoder {
	val decoder: Mp3Decoder

	def getFile(f: File) = {
		if (f.extension == "flac") decoder.decode(f) else f
	}
}