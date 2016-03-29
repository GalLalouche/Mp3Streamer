package common.io

import common.rich.RichT._
/** Either a file or a dir */
abstract class PathRef {
  def path: String
  def /(name: String): PathRef
  def /(): DirectoryRef
  def name: String
  override def toString: String = s"${this.simpleName}: $name"
}

/** a place holder for a directory */
private final class TempRef(dir: DirectoryRef) extends PathRef {
  override def /(name: String): DirectoryRef =
  throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def path: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def name: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def /(): DirectoryRef = dir
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
  def /() = throw new UnsupportedOperationException(s"file <$path> is not a directory")
}

trait DirectoryRef extends PathRef {
  def addFile(name: String): FileRef
  def getFile(name: String): Option[FileRef]
  def addSubDir(name: String): DirectoryRef
  def getDir(name: String): Option[DirectoryRef]
  def /(name: String): PathRef = getFile(name).orElse(getDir(name).map(new TempRef(_))).get
  /** Files and dirs */
  def paths: Seq[PathRef]
  def dirs: Seq[DirectoryRef] = paths collect { case e: DirectoryRef => e }
  def files: Seq[FileRef] = paths collect { case e: FileRef => e }
  def deepDirs: Seq[DirectoryRef] = dirs ++ (dirs flatMap (_.deepDirs))
  def deepFiles: Seq[FileRef] = files ++ (dirs flatMap (_.deepFiles))
  override def /(): DirectoryRef = this
}
