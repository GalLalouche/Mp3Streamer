package common.io

import java.time.LocalDateTime

import scala.collection.mutable

/** For testing; keeps the file in memory. Faster, and no need to clean up afterwards */
private class MemoryFile(parent: MemoryDir, val name: String) extends FileRef {
  override type F = MemoryFile
  private var content: String = ""
  private var lastUpdatedTime: LocalDateTime = _
  touch()
  private def touch() {
    lastUpdatedTime = LocalDateTime.now
  }
  override def bytes: Array[Byte] = content.getBytes
  override def write(bs: Array[Byte]) = {
    write(new String(bs))
  }
  override def write(s: String) = {
    content = s
    touch()
    this
  }
  override def appendLine(line: String) = {
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
  override val lastModified = LocalDateTime.now()
  override def dirs: Seq[DirectoryRef] = dirsByName.values.toSeq.sortBy(_.name)
  override def files: Seq[FileRef] = filesByName.values.toSeq.sortBy(_.name)
}
private class ConsDir(parent: MemoryDir, val name: String) extends MemoryDir(parent.path + "/" + name)
class MemoryRoot extends MemoryDir("/") {
  override def name: String = "/"
}
