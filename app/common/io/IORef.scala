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

trait IOSystem extends RefSystem {
  override type S = IOSystem
  override type P = IOPath
  override type F = IOFile
  override type D = IODirectory
}

abstract class IOPath(f: File) extends PathRef {
  override type S = IOSystem
  override def path = f.getAbsolutePath
  override def name = f.getName
  override def parent = IODirectory(f.getParent)
}

/** For production; actual files on the disk */
case class IOFile(file: File) extends IOPath(file) with FileRef {
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
  override def readAll: String = rich.readAll
  override def lastModified: LocalDateTime = file |> FileUtils.lastModified
  override def size = file.length
  override def exists = file.exists
  override def delete: Boolean = file.delete()
}

case class IODirectory(file: File) extends IOPath(file) with DirectoryRef {
  lazy val dir: Directory = Directory(file)
  def this(path: String) = this(new File(path).getAbsoluteFile)
  override def addFile(name: String) = IOFile(dir addFile name)
  private def optionalFile(name: String) = Some(new File(dir.dir, name)).filter(_.exists)
  override def getDir(name: String) =
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(new Directory(e)))
  override def addSubDir(name: String) = new IODirectory(dir addSubDir name)
  override def getFile(name: String) = optionalFile(name).map(IOFile)
  override def dirs = dir.dirs.map(new IODirectory(_))
  override def files = dir.files.map(IOFile)
  override def lastModified: LocalDateTime = dir.dir |> FileUtils.lastModified
  override def hasParent = file.getParentFile != null
}
object IODirectory {
  def apply(str: String): IODirectory = apply(Directory(str))
  def apply(dir: Directory): IODirectory = new IODirectory(dir)
}
