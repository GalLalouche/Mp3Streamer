package decoders

import common.io.FileRef
import decoders.CodecType._

trait Encoder {
  /**
   * Decodes an audio file to a different format
   * @param srcFile The original file
   * @param dstFile The destination file that will be created
   * @param dstType The type of the new file
   */
  protected def encode(srcFile: FileRef, dstFile: FileRef, dstType: CodecType): Unit
}
