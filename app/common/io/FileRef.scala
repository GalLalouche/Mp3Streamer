package common.io

import java.time.LocalDateTime

import common.rich.RichT._
/** Either a file or a dir */
abstract class PathRef {
  def path: String
  def name: String
  override def toString: String = s"${this.simpleName}: $name"
}

/** a place holder for a directory */
private final class TempRef(dir: DirectoryRef) extends PathRef {
  throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def path: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
  override def name: String =
    throw new UnsupportedOperationException(s"This instance is only a placeholder for dir <$dir>")
}

/** must exist */
trait FileRef extends PathRef {
  type F <: FileRef
  def bytes: Array[Byte]
  def write(s: String): F
  def write(bs: Array[Byte]): F
  def appendLine(line: String): F
  def readAll: String
  final def lines: Seq[String] = {
    val content = readAll
    if (content.isEmpty) Nil
    else content split "\n"
  }
  final def extension: String = {
    val i = name.lastIndexOf('.')
    if (i == -1) "" else name.substring(i + 1).toLowerCase
  }

  def lastModified: LocalDateTime
}

trait DirectoryRef extends PathRef {
  def addFile(name: String): FileRef
  def getFile(name: String): Option[FileRef]
  def addSubDir(name: String): DirectoryRef
  def getDir(name: String): Option[DirectoryRef]
  def dirs: Seq[DirectoryRef]
  def files: Seq[FileRef]
  def deepDirs: Seq[DirectoryRef] = dirs ++ (dirs flatMap (_.deepDirs))
  def deepFiles: Seq[FileRef] = files ++ (dirs flatMap (_.deepFiles))
  def lastModified: LocalDateTime
}
