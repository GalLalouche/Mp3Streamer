package mains

import java.io.File

import com.google.common.annotations.VisibleForTesting
import org.jline.utils.Levenshtein

import scala.sys.process.Process

import common.io.{DirectoryRef, IODirectory, PathRef}
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichString._

private object IOUtils {
  /** Opens Windows explorer with the file in focus */
  def focus(f: File): Unit =
    Process(s"""explorer.exe /select,"${f.getAbsolutePath.simpleReplace("/", "\\")}"""").!

  /**
   * Windows can't parse Unicode path names correctly it seems, instead it replaces the Unicode
   * characters with "?".
   */
  def decodeFile(path: String): File = {
    val file = new File(path)
    new File(decode(IODirectory(file.getParentFile), file.getName).path)
  }

  @VisibleForTesting
  private[mains] def decode(parent: DirectoryRef, fileName: String): PathRef =
    parent.getFile(fileName).getOrElse {
      require(fileName.contains('?'), "Can only attempt to decode files with '?' in their name")
      require(
        parent.path.contains('?').isFalse,
        "Can only attempt to decode files without '?' in their parent's path",
      )
      parent.paths.minBy(f => Levenshtein.distance(f.name, fileName))
    }
}
