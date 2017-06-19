package common.io

import java.time.LocalDateTime

import common.rich.RichT._

trait RefSystem { self =>
  type S <: RefSystem
  type P <: PathRef {type S = self.S}
  type F <: FileRef {type S = self.S}
  type D <: DirectoryRef {type S = self.S}
}
/** Either a file or a dir */
trait PathRef {
  type S <: RefSystem
  def path: String
  def name: String
  override def toString: String = s"${this.simpleName}: $name"
  def parent: S#D
}

/** must exist */
trait FileRef extends PathRef {
  type S <: RefSystem
  def bytes: Array[Byte]
  def write(s: String): S#F
  def write(bs: Array[Byte]): S#F
  def appendLine(line: String): S#F
  def readAll: String
  final def lines: Seq[String] = {
    val content = readAll
    if (content.isEmpty) Nil
    else content split "\n"
  }
  final def extension: String = {
    val i = name.lastIndexOf('.')
    if (i == -1) "" else name.substring(i + 1).toLowerCase
  }

  def lastModified: LocalDateTime
}

trait DirectoryRef extends PathRef { self =>
  type S <: RefSystem
  def addFile(name: String): S#F
  def getFile(name: String): Option[S#F]
  def addSubDir(name: String): S#D
  def getDir(name: String): Option[S#D]
  def dirs: Seq[S#D]
  def files: Seq[S#F]
  def deepDirs: Seq[S#D] = dirs ++ dirs.flatMap(_.deepDirs).asInstanceOf[Seq[S#D]]
  def deepFiles: Seq[S#F] = files ++ dirs.flatMap(_.deepFiles).asInstanceOf[Seq[S#F]]
  def lastModified: LocalDateTime
}
