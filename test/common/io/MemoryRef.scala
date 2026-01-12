package common.io

import java.io.InputStream
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._

import common.rich.primitives.RichString._

trait MemorySystem extends RefSystem {
  override type S = MemorySystem
  override type P = MemoryPath
  override type F = MemoryFile
  override type D = MemoryDir
}

sealed trait MemoryPath extends PathRef {
  override type S = MemorySystem
}

case class MemoryFile(parent: MemoryDir, name: String) extends FileRef with MemoryPath {
  private var content: String = ""
  private var lastUpdatedTime: LocalDateTime = _
  touch()
  private def touch(): Unit =
    lastUpdatedTime = LocalDateTime.now
  override def bytes = content.getBytes
  override def write(bs: Array[Byte]): MemoryFile =
    write(new String(bs))
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
  override def inputStream: InputStream = content.toInputStream
  override def path: String = parent.path + "/" + name
  override def lastModified = lastUpdatedTime
  override def size = bytes.length
  override def exists = parent.files.exists(_.name == this.name)
  override def delete = parent.deleteFile(this.name)

  override val creationTime = LocalDateTime.now()
  override def lastAccessTime = LocalDateTime.now()
}

sealed abstract class MemoryDir(val path: String) extends DirectoryRef with MemoryPath {
  private val filesByName = new ConcurrentHashMap[String, MemoryFile]().asScala
  private val dirsByName = new ConcurrentHashMap[String, MemoryDir]().asScala

  override def getFile(name: String) = filesByName.get(name)
  override def addFile(name: String) = getFile(name).getOrElse {
    val $ = MemoryFile(this, name)
    filesByName += ((name, $))
    $
  }
  override def getDir(name: String): Option[MemoryDir] = dirsByName.get(name)
  override def addSubDir(name: String) = addSubDir(name, LocalDateTime.now())
  def addSubDir(name: String, lastModified: LocalDateTime): MemoryDir = getDir(name).getOrElse {
    val $ = SubDir(this, name, lastModified)
    dirsByName += ((name, $))
    $
  }
  override def dirs: Iterator[MemoryDir] = dirsByName.values.toSeq.sortBy(_.name).iterator
  override def files: Iterator[MemoryFile] = filesByName.values.toSeq.sortBy(_.name).iterator

  def deleteFile(name: String): Boolean = {
    val hasFile = filesByName.contains(name)
    if (hasFile)
      filesByName -= name
    hasFile
  }
  override def clear(): MemoryDir = {
    filesByName.clear()
    dirsByName.values.foreach(_.clean())
    dirsByName.clear()
    this
  }
  private def clean(): Unit = {
    filesByName.clear()
    dirsByName.values.foreach(_.clean())
  }
}
private case class SubDir(
    parent: MemoryDir,
    name: String,
    override val lastModified: LocalDateTime = LocalDateTime.now(),
) extends MemoryDir(parent.path + "/" + name) {
  override def hasParent = true
}
class MemoryRoot extends MemoryDir("/") {
  override def name: String = "/"
  override def parent = throw new UnsupportedOperationException("MemoryRoot has no parent")
  override def hasParent = false
  override val path = s"root(${System.identityHashCode(this)})/"
  override val lastModified: LocalDateTime = LocalDateTime.now()
}
