package mains.splitter

import java.io.File

import common.rich.path.RichFile._
import common.rich.path.{Directory, RichFileUtils}

import scala.sys.process.Process

private object CueTools {
  private val exe = """C:\Program Files (x86)\CUETools_2.1.5\CUETools.exe"""
  private def clean(dir: Directory) {
    println("Moving flac files to parent dir")
    dir.files.find(_.name == "00. (HTOA).flac").foreach(_.delete())
    dir.files.filter(_.extension == "flac").foreach(f => RichFileUtils.move(f, dir.parent))
    println("Deleting convert dir")
    dir.deleteAll()
  }
  def split(cueFile: File) {
    println("Invoking CUETools")
    Process(exe, Seq("/convert", s""""${cueFile.getAbsolutePath}"""")).!!
    clean(Directory(cueFile.getParent) / "convert" /)
  }
}
