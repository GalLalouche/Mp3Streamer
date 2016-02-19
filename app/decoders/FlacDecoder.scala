package decoders

import java.io.File

import common.rich.path.RichFile.richFile

trait FlacDecoder extends Mp3Encoder {
  def encodeFileIfNeeded(f: File) = if (f.extension.toLowerCase == "flac") encode(f) else f
}