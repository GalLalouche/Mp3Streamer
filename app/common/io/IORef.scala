package common.io

import java.io.{File, FileInputStream, InputStream, IOException}
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.{Clock, LocalDateTime, ZoneId}

import better.files.{File => BFile, FileExtensions}
import rx.lang.scala.Observable

import common.rich.RichT._
import common.rich.path.{Directory, RichFile}

private[this] object FileUtils {
  private val currentZone = Clock.systemDefaultZone().getZone
  def lastModified(f: File): LocalDateTime = LocalDateTime.ofInstant(
    Files.readAttributes(f.toPath, classOf[BasicFileAttributes]).lastModifiedTime().toInstant,
    currentZone,
  )
}

trait IOSystem extends RefSystem {
  override type S = IOSystem
  override type P = IOPath
  override type F = IOFile
  override type D = IODirectory
}

sealed abstract class IOPath(f: File) extends PathRef {
  override type S = IOSystem
  val file: File = f
  def better: BFile = file.toScala
  override def path = f.getAbsolutePath.replace(File.separatorChar, '/')
  override def name = f.getName
  override def parent = IODirectory(f.getParent)
}

object IOPathRefFactory extends PathRefFactory {
  override def parsePath(path: String): IOPath = {
    val file = new File(path)
    if (file.isDirectory)
      IODirectory(file)
    else if (file.isFile)
      IOFile(file)
    else
      throw new IOException(s"Path <$path> is not a valid file or directory")
  }
  override def parseFilePath(path: String): IOFile = parsePath(path).asInstanceOf[IOFile]
  override def parseDirPath(path: String): IODirectory = parsePath(path).asInstanceOf[IODirectory]
}

/** For production; actual files on the disk */
case class IOFile(_file: File) extends IOPath(_file) with FileRef {
  private lazy val rich = RichFile(file)
  override def bytes: Array[Byte] = rich.bytes
  override def write(bs: Array[Byte]) = {
    rich.write(bs)
    this
  }
  override def write(s: String) = {
    rich.write(s)
    this
  }
  override def appendLine(line: String) = {
    rich.appendLine(line)
    this
  }
  override def readAll: String = rich.readAll
  override def inputStream: InputStream = new FileInputStream(file)
  override def lastModified: LocalDateTime = file |> FileUtils.lastModified
  override def size = file.length
  override def exists = file.exists
  override def delete: Boolean = file.delete()

  override def creationTime = LocalDateTime.ofInstant(
    Files.readAttributes(file.toPath, classOf[BasicFileAttributes]).creationTime().toInstant,
    ZoneId.systemDefault(),
  )
  override def lastAccessTime = LocalDateTime.ofInstant(
    Files.readAttributes(file.toPath, classOf[BasicFileAttributes]).lastAccessTime().toInstant,
    ZoneId.systemDefault(),
  )
}
object IOFile {
  def apply(str: String): IOFile = apply(new File(str))
}

class IODirectory private (val dir: Directory) extends IOPath(dir.dir) with DirectoryRef {
  override def hasParent = dir.dir.getParent != null
  override def addFile(name: String) = IOFile(dir.addFile(name))
  private def optionalFile(name: String): Option[File] =
    Option(new File(dir.dir, name)).filter(_.exists)
  override def getDir(name: String): Option[IODirectory] =
    // TODO redundant check in Directory.apply
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(Directory(e)))
  override def addSubDir(name: String): IODirectory = new IODirectory(dir.addSubDir(name))
  override def getFile(name: String) = optionalFile(name).map(IOFile.apply)
  override def dirs: Iterator[IODirectory] = dir.dirs.map(new IODirectory(_))
  override def files = dir.files.map(IOFile.apply)
  override def paths: Iterator[IOPath] =
    dir.listFiles.iterator.map(f => if (f.isDirectory) new IODirectory(Directory(f)) else IOFile(f))
  override def lastModified: LocalDateTime = dir.dir |> FileUtils.lastModified
  override def deepDirs: Iterator[IODirectory] = dir.deepDirs.map(new IODirectory(_))
  override def deepDirsObservable: Observable[IODirectory] =
    dir.deepDirsObservable.map(d => new IODirectory(d._1))
  override def deepFiles: Iterator[IOFile] = dir.deepFiles.map(new IOFile(_))

  override def equals(other: Any): Boolean = other match {
    case that: IODirectory => this.isInstanceOf[IODirectory] && dir == that.dir
    case _ => false
  }

  override def hashCode(): Int = dir.hashCode()
  override def toString = s"IODirectory($dir)"
  override def clear(): IODirectory = {
    dir.clear()
    this
  }
}
object IODirectory {
  def apply(file: File): IODirectory = apply(Directory(file))
  def apply(str: String): IODirectory = apply(Directory(str))
  def apply(dir: Directory): IODirectory = new IODirectory(dir)
}
