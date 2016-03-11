package common.io

/** Either a file or a dir */
abstract class PathRef {
  def path: String
  def /(name: String): PathRef
  def name: String
}

/** a place holder for a directory */
private final class TempRef(dir: DirectoryRef) extends PathRef {
  override def /(name: String): DirectoryRef = dir
  override def path: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def name: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
}

/** must exist */
trait FileRef extends PathRef {
  def write(s: String): Unit
  def readAll: String
  final def lines: Seq[String] = {
    val content = readAll
    if (content.isEmpty) Nil
    else content split "\n"
  }
  final def extension = {
    val i = name.lastIndexOf('.')
    if (i == -1) "" else name.substring(i + 1).toLowerCase
  }
  def /(name: String) = throw new UnsupportedOperationException(s"file <$path> is not a directory")
}

trait DirectoryRef extends PathRef {
  def addFile(name: String): FileRef
  def getFile(name: String): Option[FileRef]
  def addSubDir(name: String): DirectoryRef
  def getDir(name: String): Option[DirectoryRef]
  def /(name: String): PathRef = getFile(name).orElse(getDir(name).map(new TempRef(_))).get
}
