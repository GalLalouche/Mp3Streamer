package decoders

import java.io.File

import common.rich.path.RichFile.richFile

trait Decoder extends Mp3Decoder {
  def decodeFileIfNeeded(f: File) = if (f.extension.toLowerCase == "flac") decode(f) else f
}