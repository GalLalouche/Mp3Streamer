package common.io

import scala.collection.mutable

/** For testing; keeps the file in memory. Faster, and no need to clean up afterwards */
private class MemoryFile(parent: MemoryDir, val name: String) extends FileRef {
  var readAll: String = ""
  override def bytes: Array[Byte] = readAll.getBytes
  override def write(bs: Array[Byte]): FileRef = {
    readAll = new String(bs)
    this
  }
  override def write(s: String) = {
    readAll = s
    this
  }
  override def path: String = parent.path + "/" + name
}

abstract sealed class MemoryDir(val path: String) extends DirectoryRef {
  private val filesByName = mutable.Map[String, MemoryFile]()
  private val dirsByName = mutable.Map[String, ConsDir]()
  override def getFile(name: String): Option[FileRef] = filesByName get name
  override def addFile(name: String): FileRef = getFile(name).getOrElse {
    val $ = new MemoryFile(this, name)
    filesByName += ((name, $))
    $
  }
  override def getDir(name: String): Option[DirectoryRef] = dirsByName get name
  override def addSubDir(name: String): DirectoryRef = getDir(name).getOrElse {
    val $ = new ConsDir(this, name)
    dirsByName += ((name, $))
    $
  }
  override def paths = (filesByName.values ++ dirsByName.values).toStream
}
private class ConsDir(parent: MemoryDir, val name: String) extends MemoryDir(parent.path + "/" + name)
class Root extends MemoryDir("/") {
  override def name: String = "/"
}
