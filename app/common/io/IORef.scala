package common.io

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.{Clock, LocalDateTime}

import common.rich.RichT._
import common.rich.path.{Directory, RichFile}

private[this] object FileUtils {
  private val currentZone = Clock.systemDefaultZone().getZone
  def lastModified(f: File): LocalDateTime = {
    LocalDateTime.ofInstant(Files.readAttributes(f.toPath, classOf[BasicFileAttributes]).lastModifiedTime().toInstant, currentZone)
  }
}
/** For production; actual files on the disk */
class IOFile(val file: File) extends FileRef {
  override type F = IOFile
  private lazy val rich = RichFile(file)
  override def bytes: Array[Byte] = rich.bytes
  override def write(bs: Array[Byte]) = {
    rich write bs
    this
  }
  override def write(s: String) = {
    rich write s
    this
  }
  override def appendLine(line: String) = {
    rich appendLine line
    this
  }
  override def path: String = rich.path
  override def readAll: String = rich.readAll
  override def name: String = rich.name
  override def lastModified: LocalDateTime = file |> FileUtils.lastModified
}

class IODirectory(val dir: Directory) extends DirectoryRef {
  override type F = IOFile
  override type D = IODirectory
  def this(path: String) = this(Directory(path))
  override def addFile(name: String) = new IOFile(dir addFile name)
  private def optionalFile(name: String) = Some(new File(dir.dir, name)).filter(_.exists)
  override def getDir(name: String) =
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(new Directory(e)))
  override def addSubDir(name: String) = new IODirectory(dir addSubDir name)
  override def getFile(name: String) = optionalFile(name).map(new IOFile(_))
  override def path = dir.path
  override def name = dir.name
  override def dirs = dir.dirs.map(new IODirectory(_))
  override def files = dir.files.map(new IOFile(_))
  override def lastModified: LocalDateTime = dir.dir |> FileUtils.lastModified
}
object IODirectory {
  def apply(str: String): IODirectory = this (Directory(str))
  def apply(dir: Directory): IODirectory = new IODirectory(dir)
}
