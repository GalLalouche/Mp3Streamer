package common.io

import java.time.LocalDateTime

import scala.collection.mutable

abstract sealed class MemoryFile(parent: MemoryDir, val name: String) extends FileRef {
  override type F = MemoryFile
  private var content: String = ""
  private var lastUpdatedTime: LocalDateTime = _
  touch()
  private def touch() {
    lastUpdatedTime = LocalDateTime.now
  }
  override def bytes = content.getBytes
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
  override def lastModified = lastUpdatedTime
}

private class MemoryFileImpl(parent: MemoryDir, name: String) extends MemoryFile(parent, name)

abstract sealed class MemoryDir(val path: String) extends DirectoryRef {
  override type F = MemoryFile
  override type D = MemoryDir
  private val filesByName = mutable.Map[String, MemoryFile]()
  private val dirsByName = mutable.Map[String, SubDir]()
  override def getFile(name: String) = filesByName get name
  override def addFile(name: String) = getFile(name).getOrElse {
    val $ = new MemoryFileImpl(this, name)
    filesByName += ((name, $))
    $
  }
  override def getDir(name: String): Option[MemoryDir] = dirsByName get name
  override def addSubDir(name: String) = getDir(name).getOrElse {
    val $ = new SubDir(this, name)
    dirsByName += ((name, $))
    $
  }
  override val lastModified = LocalDateTime.now()
  override def dirs: Seq[MemoryDir] = dirsByName.values.toSeq.sortBy(_.name)
  override def files = filesByName.values.toSeq.sortBy(_.name)
}
private class SubDir(parent: MemoryDir, val name: String) extends MemoryDir(parent.path + "/" + name)
class MemoryRoot extends MemoryDir("/") {
  override def name: String = "/"
}
