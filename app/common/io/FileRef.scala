package common.io

import java.io.File

import common.rich.path.RichFile

import scala.collection.mutable

/** A wrapper of a file to allow tests to use in-memory representations */
trait FileSystem {
  def getFile(path: String): FileRef
}

trait FileRef {
  def exists: Boolean
  def write(s: String): Unit
  def lines: Seq[String]
  def extension: String
  def path: String
  def create(): Unit
}

/** For production; actual files on the disk */
trait IOFileSystem extends FileSystem {
  override def getFile(path: String): FileRef = new java.io.File(path)

  protected implicit class IOFileRef(file: File) extends FileRef {
    override def exists = file.exists
    override def lines = RichFile(file).lines
    override def write(s: String) { RichFile(file).write(s) }
    override def extension: String = file.extension
    override def path: String = file.path
    override def create() { file.createNewFile() }
  }
}

/** For testing; keeps the file in memory. Faster, and no need to clean up afterwards */
trait MemoryFileSystem extends FileSystem {
  private val fileSystem = mutable.HashMap[String, MemoryFile]()
  override def getFile(path: String): FileRef = fileSystem.get(path).getOrElse(new MemoryFile(path))

  private class MemoryFile(name: String) extends FileRef {
    var content: String = ""
    override def exists = fileSystem contains name
    override def lines: Seq[String] = content split "\n"
    override def write(s: String) {
      require(exists, "Can't write to a non-existing file; this will fail in production")
      content = s
    }
    override def extension: String = name.dropWhile(_ != '.')
    override def path: String = name
    override def create() { fileSystem += name -> this }
  }
}
