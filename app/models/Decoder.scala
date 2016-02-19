package models

import java.io.File

import common.rich.path.RichFile.richFile
import decoders.Mp3Decoder

trait Decoder extends Mp3Decoder {
  def decodeFileIfNeeded(f: File) = if (f.extension.toLowerCase == "flac") decode(f) else f
}