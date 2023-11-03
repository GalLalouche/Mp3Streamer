package decoders

import com.google.inject.ImplementedBy
import common.io.FileRef

@ImplementedBy(classOf[DbPowerampCodec])
private trait Encoder {
  /**
   * Decodes an audio file to a different format
   * @param srcFile
   *   The original file
   * @param dstFile
   *   The destination file that will be created
   * @param dstType
   *   The type of the new file
   */
  def encode(srcFile: FileRef, dstFile: FileRef, dstType: CodecType): Unit
}
