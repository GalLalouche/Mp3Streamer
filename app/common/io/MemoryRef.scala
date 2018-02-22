package common.io

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

import common.json.{Jsonable, ToJsonableOps}
import common.rich.primitives.RichOption._
import play.api.libs.json.{JsString, JsValue}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

trait MemorySystem extends RefSystem {
  override type S = MemorySystem
  override type P = MemoryPath
  override type F = MemoryFile
  override type D = MemoryDir
}
trait MemoryPath extends PathRef {
  override type S = MemorySystem
}

case class MemoryFile(parent: MemoryDir, name: String) extends FileRef with MemoryPath {
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
  override def size = bytes.length
  override def exists = parent.files.exists(_.name == this.name)
  override def delete(): Unit = parent deleteFile this
}

object MemoryFile {
  implicit def JsonableMemoryFile(implicit rootProvider: MemoryRootProvider): Jsonable[MemoryFile] =
    new Jsonable[MemoryFile] with ToJsonableOps {
      override def jsonify(t: MemoryFile): JsValue = t.path.jsonify
      override def parse(json: JsValue): MemoryFile = {
        val (fileName :: reversedDir) = json.as[String].split("/").reverse.toList
        // TODO why can't you parseJsonable out of a string that's a JsString?
        val dirPath = JsString(reversedDir.reverse.mkString("/"))
        dirPath.parse[MemoryDir].getFile(fileName)
            .getOrThrow(s"Failed to parse MemoryFile JSON: The file <$fileName> no longer exists")
      }
    }
}

abstract sealed class MemoryDir(val path: String) extends DirectoryRef with MemoryPath {
  private val filesByName = new ConcurrentHashMap[String, MemoryFile]().asScala
  private val dirsByName = new ConcurrentHashMap[String, MemoryDir]().asScala
  override def getFile(name: String) = filesByName get name
  override def addFile(name: String) = getFile(name).getOrElse {
    val $ = MemoryFile(this, name)
    filesByName += ((name, $))
    $
  }
  override def getDir(name: String): Option[MemoryDir] = dirsByName get name
  override def addSubDir(name: String) = getDir(name).getOrElse {
    val $ = SubDir(this, name)
    dirsByName += ((name, $))
    $
  }
  override val lastModified = LocalDateTime.now()
  override def dirs: Seq[MemoryDir] = dirsByName.values.toSeq.sortBy(_.name)
  override def files = filesByName.values.toSeq.sortBy(_.name)

  private[io] def deleteFile(file: MemoryFile): Unit = filesByName.remove(file.name)
  private[io] def deleteDir(file: MemoryDir): Unit = dirsByName.remove(file.name)
  override def deleteAll(): Unit = parent deleteDir this
}

object MemoryDir {
  implicit def JsonableMemoryDir(implicit rootProvider: MemoryRootProvider): Jsonable[MemoryDir] =
    new Jsonable[MemoryDir] with ToJsonableOps {
      override def jsonify(t: MemoryDir): JsValue = t.path.jsonify
      override def parse(json: JsValue): MemoryDir = {
        // TODO replace with a fold
        @tailrec
        def aux(paths: List[String], dir: MemoryDir): MemoryDir = paths match {
          case Nil => dir
          case d :: xs => aux(xs, dir.getDir(d)
              .getOrThrow(s"Failed to parse MemoryDir JSON: The dir <$d> (from path <$json> no longer exists"))
        }
        aux(json.as[String].split("/").tail.toList, rootProvider.rootDirectory)
      }
    }
}
private case class SubDir(parent: MemoryDir, name: String) extends MemoryDir(parent.path + "/" + name) {
  override def hasParent = true
}
class MemoryRoot extends MemoryDir("/") {
  override def name: String = "/"
  override def parent = throw new UnsupportedOperationException("MemoryRoot has no parent")
  override def hasParent = false
  override val path = s"root(${System.identityHashCode(this)})"
}
