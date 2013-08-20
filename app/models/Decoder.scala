package models

import java.io.File

import common.path.Path.richPath
import decoders.Mp3Decoder

trait Decoder {
	val decoder: Mp3Decoder

	def getFile(f: File) = {
		if (f.extension == "flac") decoder.decode(f) else f
	}
}