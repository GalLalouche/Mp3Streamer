package common.io

import java.io.InputStream
import java.time.LocalDateTime

import common.rich.RichT._

trait RefSystem {self =>
  type S <: RefSystem
  type P <: PathRef {type S = self.S}
  type F <: FileRef {type S = self.S}
  type D <: DirectoryRef {type S = self.S}
}

/** Either a file or a directory. */
trait PathRef {
  type S <: RefSystem
  def path: String
  def name: String
  override def toString: String = s"${this.simpleName}: $path"
  def parent: S#D
  def parents: Seq[S#D]
}

/** Must exist. */
trait FileRef extends PathRef {
  type S <: RefSystem

  def size: Long
  def bytes: Array[Byte]
  def write(s: String): S#F
  def write(bs: Array[Byte]): S#F
  def clear(): FileRef = write("")
  def appendLine(line: String): S#F
  def readAll: String
  final def lines: Seq[String] = {
    // Splitting an empty string returns [""].
    val content = readAll
    if (content.isEmpty) Nil else content split "\n"
  }
  def inputStream: InputStream

  final lazy val extension: String = {
    val i = name.lastIndexOf('.')
    if (i == -1) "" else name.substring(i + 1).toLowerCase
  }
  final lazy val nameWithoutExtension: String = name.dropRight(extension.length)

  def lastModified: LocalDateTime
  def creationTime: LocalDateTime
  def lastAccessTime: LocalDateTime

  def exists: Boolean
  def delete: Boolean

  override def parents = parent +: parent.parents.asInstanceOf[Seq[S#D]]
}

/** Must exist. */
trait DirectoryRef extends PathRef {self =>
  type S <: RefSystem
  def addFile(name: String): S#F
  def getFile(name: String): Option[S#F]
  def addSubDir(name: String): S#D
  def getDir(name: String): Option[S#D]
  def dirs: Seq[S#D]
  def files: Seq[S#F]
  def paths: Seq[S#P] = dirs.++(files).asInstanceOf[Seq[S#P]]
  def deepDirs: Seq[S#D] = dirs ++ dirs.flatMap(_.deepDirs).asInstanceOf[Seq[S#D]]
  def deepFiles: Seq[S#F] = files ++ dirs.flatMap(_.deepFiles).asInstanceOf[Seq[S#F]]
  def lastModified: LocalDateTime
  def hasParent: Boolean
  // TODO freaking unfoldables already
  override def parents =
    Stream.iterate(Option(parent))(p => if (p.get.hasParent) Some(p.get.parent.asInstanceOf[S#D]) else None)
        .takeWhile(_.isDefined)
        .map(_.get)
  /** Returns all directories between this and dir. Throws if dir is not a parent of this. */
  def relativize(dir: S#D): Seq[S#D] = {
    val ps = parents.span(_ != dir)
    require(ps._2.nonEmpty, s"<$dir> is not a parent of <$this>")
    ps._1.toList
  }
}
