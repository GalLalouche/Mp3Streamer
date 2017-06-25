package common.io

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.{Clock, LocalDateTime}

import common.rich.RichT._
import common.rich.path.{Directory, RichFile, RichPath}

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
  private lazy val rp = RichPath.richPath(f)
  override type S = IOSystem
  override def path = f.getAbsolutePath
  override def name = f.getName
  override def parent = IODirectory(rp.parent)
}

/** For production; actual files on the disk */
class IOFile(val file: File) extends IOPath(file) with FileRef {
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
}

class IODirectory(file: File) extends IOPath(file) with DirectoryRef {
  lazy val dir: Directory = Directory(file)
  def this(path: String) = this(new File(path))
  override def addFile(name: String) = new IOFile(dir addFile name)
  private def optionalFile(name: String) = Some(new File(dir.dir, name)).filter(_.exists)
  override def getDir(name: String) =
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(new Directory(e)))
  override def addSubDir(name: String) = new IODirectory(dir addSubDir name)
  override def getFile(name: String) = optionalFile(name).map(new IOFile(_))
  override def dirs = dir.dirs.map(new IODirectory(_))
  override def files = dir.files.map(new IOFile(_))
  override def lastModified: LocalDateTime = dir.dir |> FileUtils.lastModified
}
object IODirectory {
  def apply(str: String): IODirectory = this (Directory(str))
  def apply(dir: Directory): IODirectory = new IODirectory(dir)
}
