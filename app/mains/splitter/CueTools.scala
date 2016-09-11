package mains.splitter

import java.io.File
import java.nio.file.Files

import common.rich.path.Directory
import common.rich.path.RichFile._

import scala.sys.process.Process

private object CueTools {
  private val exe = """C:\Program Files (x86)\CUETools_2.1.5\CUETools.exe"""
  private def clean(dir: Directory) {
    dir.files
        .filter(_.extension == "flac")
        .foreach(f => Files.move(f.toPath, new File(dir.getParentFile, f.name).toPath)) // move files up
    dir.deleteAll
  }
  def split(cueFile: File) {
    Process(exe, Seq("/convert", s""""${cueFile.getAbsolutePath}"""")).!!
    clean(Directory(cueFile.getParent) / "convert" /)
  }
}
