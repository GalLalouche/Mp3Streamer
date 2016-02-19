package decoders

import java.io.File

import common.rich.path.RichFile.richFile

trait FlacDecoder extends Mp3Encoder {
  def decodeFileIfNeeded(f: File) = if (f.extension.toLowerCase == "flac") decode(f) else f
}