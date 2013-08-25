package models

import java.io.File

import common.path.Path.richPath
import decoders.Mp3Decoder

trait Decoder extends Mp3Decoder {
	def decodeFileIfNeeded(f: File) = {
		if (f.extension == "flac") decode(f) else f
	}
}