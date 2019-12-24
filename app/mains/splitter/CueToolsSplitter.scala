package mains.splitter

import java.io.File

import scala.language.postfixOps
import scala.sys.process.Process

import common.rich.path.Directory

private object CueToolsSplitter extends CueSplitter {
  private val CueToolsExe = """C:\Program Files (x86)\CUETools_2.1.5\CUETools.exe"""
  override def apply(cueFile: File, flacFile: File): Directory = {
    Process(CueToolsExe, Seq("/convert", s""""${cueFile.getAbsolutePath}"""")).!!
    Directory(cueFile.getParent) / "convert" /
  }
}
