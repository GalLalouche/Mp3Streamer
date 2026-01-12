package mains

import java.io.File

import com.google.common.annotations.VisibleForTesting
import org.jline.utils.Levenshtein

import scala.sys.process.Process

import common.io.{DirectoryRef, IODirectory, PathRef}
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichString._

private object IOUtils {
  /** Opens Windows explorer with the file in focus, i.e., it opens its parent. */
  def focus(f: File): Unit = {
    val windowsPath = s""""${f.getAbsolutePath.simpleReplace("/", "\\")}""""
    Process(Vector("cmd.exe", "/C", "explorer.exe /select," + windowsPath)).!
  }

  /**
   * Windows can't parse Unicode path names correctly it seems, instead it replaces the Unicode
   * characters with "?".
   */
  def decodeFile(path: String): File = {
    val file = new File(path)
    require(file.getParentFile != null, s"File has no parent: $path; path is invalid or root.")
    require(file.getParentFile.exists(), s"Parent directory does not exist: ${file.getParent}")
    new File(decode(IODirectory(file.getParentFile), file.getName).path)
  }

  @VisibleForTesting
  private[mains] def decode(parent: DirectoryRef, fileName: String): PathRef =
    parent.getFile(fileName).getOrElse {
      if (fileName.contains('?')) {
        require(
          parent.path.contains('?').isFalse,
          "Can only attempt to decode files without '?' in their parent's path",
        )
        parent.paths.minBy(f => Levenshtein.distance(f.name, fileName))
      } else
        throw new IllegalArgumentException(
          s"File <$fileName> not found in directory <${parent.path}>",
        )
    }
}
