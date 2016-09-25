package common.io

import java.time.{Clock, LocalDateTime, ZoneOffset}

import scala.collection.mutable

/** For testing; keeps the file in memory. Faster, and no need to clean up afterwards */
private class MemoryFile(parent: MemoryDir, val name: String) extends FileRef {
  private var content: String = ""
  private var lastUpdatedTime: LocalDateTime = _
  touch()
  private def touch() {
    lastUpdatedTime = LocalDateTime.now
  }
  override def bytes: Array[Byte] = content.getBytes
  override def write(bs: Array[Byte]): FileRef = {
    write(new String(bs))
  }
  override def write(s: String) = {
    content = s
    touch()
    this
  }
  override def appendLine(line: String): FileRef = {
    content += line
    touch()
    this
  }

  override def readAll: String = content
  override def path: String = parent.path + "/" + name
  override def lastModified: LocalDateTime = lastUpdatedTime
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
  override val lastModified = LocalDateTime.now()
}
private class ConsDir(parent: MemoryDir, val name: String) extends MemoryDir(parent.path + "/" + name)
class MemoryRoot extends MemoryDir("/") {
  override def name: String = "/"
}
