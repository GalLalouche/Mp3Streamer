package backend.module

import com.google.inject.Inject

import common.io.{MemoryDir, MemoryFile, MemoryPath, MemoryRoot, PathRefFactory}
import common.rich.primitives.RichOption.richOption

private class MemoryPathRefFactory @Inject() (root: MemoryRoot) extends PathRefFactory {
  override def parsePath(path: String): MemoryPath = fromFilePath(path)
  override def parseFilePath(path: String): MemoryFile = parsePath(path) match {
    case file: MemoryFile => file
    case _: MemoryDir =>
      throw new IllegalArgumentException(s"Path $path is a directory, not a file")
  }
  override def parseDirPath(path: String): MemoryDir = parsePath(path) match {
    case dir: MemoryDir => dir
    case _: MemoryFile =>
      throw new IllegalArgumentException(s"Path $path is a file, not a directory")
  }

  private def fromFilePath(file: String): MemoryPath = {
    val hash = System.identityHashCode(root).toString
    file match {
      case MemoryPathRefFactory.RootRegex(rootHash) =>
        if (rootHash != hash)
          throw new IllegalArgumentException(s"Invalid root <$rootHash>, expected <$hash>")
        else
          root
      case MemoryPathRefFactory.PathRegex(rootHash, path) =>
        if (rootHash != hash)
          throw new IllegalArgumentException(s"Invalid root <$rootHash>, expected <$hash>")
        val paths = path.split("/")
        assert(paths.nonEmpty)
        paths.foldLeft(root: MemoryPath) { (d, s) =>
          d match {
            case dir: MemoryDir =>
              dir
                .getFile(s)
                .orElse(dir.getDir(s))
                .getOrThrow(s"Not path named <$s> found under <$dir>")
            case _: MemoryFile =>
              throw new IllegalArgumentException(
                s"<$d> is a file, yet there are still remaining path fragments",
              )
          }
        }
      case _ => throw new IllegalArgumentException(s"Invalid path <$file>")
    }
  }
}

object MemoryPathRefFactory {
  private val RootRegexStr = """root\((\d+)\)/"""
  private val PathRegex = s"$RootRegexStr/(.*)".r
  private val RootRegex = RootRegexStr.r
}
