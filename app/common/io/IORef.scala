package common.io
import java.io.File

import common.rich.path.{Directory, RichFile}

/** For production; actual files on the disk */
private class IOFileRef(file: File) extends FileRef {
  private val rich = RichFile(file)
  override def write(s: String) { rich.write(s) }
  override def path: String = rich.path
  override def readAll: String = rich.readAll
  override def name: String = rich.name
}

class IODirectory(dir: Directory) extends DirectoryRef {
  def this(path: String) = this(Directory(path))
  override def addFile(name: String): FileRef = new IOFileRef(dir addFile name)
  private def optionalFile(name: String) = {
    val f = new File(dir.dir, name)
    if (f.exists)
      Some(f)
    else
      None
  }
  override def getDir(name: String): Option[DirectoryRef] =
    optionalFile(name).filter(_.isDirectory).map(e => new IODirectory(new Directory(e)))
  override def addSubDir(name: String): DirectoryRef = new IODirectory(dir addSubDir name)
  override def getFile(name: String): Option[FileRef] = optionalFile(name).map(new IOFileRef(_))
  override def path: String = dir.path
  override def name: String = dir.name
}
