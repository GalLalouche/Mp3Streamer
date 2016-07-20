package common.io

import java.io.File

import common.rich.path.{Directory, RichFile}

/** For production; actual files on the disk */
class IOFile(val file: File) extends FileRef {
  private lazy val rich = RichFile(file)
  override def bytes: Array[Byte] = rich.bytes
  override def write(bs: Array[Byte]): IOFile = {
    rich.write(bs); this
  }
  override def write(s: String) = {
    rich.write(s); this
  }
  override def path: String = rich.path
  override def readAll: String = rich.readAll
  override def name: String = rich.name
}

class IODirectory(val dir: Directory) extends DirectoryRef {
  def this(path: String) = this(Directory(path))
  override def addFile(name: String): FileRef = new IOFile(dir addFile name)
  private def optionalFile(name: String) = {
    val f = new File(dir.dir, name)
    if (f.exists)
      Some(f)
    else
      None
  }
  override def getDir(name: String): Option[IODirectory] =
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(new Directory(e)))
  override def addSubDir(name: String): IODirectory = new IODirectory(dir addSubDir name)
  override def getFile(name: String): Option[IOFile] = optionalFile(name).map(new IOFile(_))
  override def path: String = dir.path
  override def paths = dir.files.map(new IOFile(_)) ++ dir.dirs.map(new IODirectory(_))
  override def name: String = dir.name
  override def dirs: Seq[IODirectory] = dir.dirs.map(new IODirectory(_))
  override def files: Seq[IOFile] = dir.files.map(new IOFile(_))
  // casting is stupid :|
  override def deepDirs: Seq[IODirectory] = super.deepDirs.asInstanceOf[Seq[IODirectory]]
  override def deepFiles: Seq[IOFile] = super.deepFiles.asInstanceOf[Seq[IOFile]]
}
object IODirectory {
  def apply(str: String): IODirectory = this (Directory(str))
  def apply(dir: Directory): IODirectory = new IODirectory(dir)
}
