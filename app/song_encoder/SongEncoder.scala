package song_encoder

import com.google.inject.ImplementedBy

import common.path.ref.FileRef

@ImplementedBy(classOf[DbPowerampCodec])
private trait SongEncoder {
  /** Decodes an audio file to mp3. */
  def encode(srcFile: FileRef, dstFile: FileRef): Unit
}
