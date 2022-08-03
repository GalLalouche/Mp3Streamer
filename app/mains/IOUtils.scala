package mains

import com.google.common.annotations.VisibleForTesting
import org.jline.utils.Levenshtein

import java.io.File

import common.io.{DirectoryRef, FileRef, IODirectory, IOFile, IOPath, PathRef}
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichString._

private object IOUtils {
  /** Opens windows explorer with the file in focus */
  def focus(f: File): Unit =
    Runtime.getRuntime.exec(
      s"""explorer.exe /select,"${f.getAbsolutePath.simpleReplace("/", "\\")}"""")

  /**
   * Windows can't pass unicode path names correctly it seems, instead it replaces the unicode characters
   * with "?".
   */
  def decodeFile(path: String): File = {
    val file = new File(path)
    new File(decode(IODirectory(file.getParentFile), file.getName).path)
  }
  @VisibleForTesting
  private[mains] def decode(parent: DirectoryRef, fileName: String): PathRef = parent.getFile(fileName).getOrElse {
    if (fileName.contains('?').isFalse)
      throw new IllegalArgumentException("Can only attempt to decode files with '?' in their name")
    if (parent.path.contains('?'))
      throw new IllegalArgumentException("Can only attempt to decode files without '?' in their parent's path")

    parent.paths.minBy(f => Levenshtein.distance(f.name, fileName))
  }
}
