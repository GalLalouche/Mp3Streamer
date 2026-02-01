package mains.splitter

import java.io.File

import scala.language.postfixOps
import scala.sys.process.Process

import common.path.ref.io.{IODirectory, IOFile}

private object CueToolsSplitter extends CueSplitter {
  private val CueToolsExe = """C:\Program Files (x86)\CUETools_2.1.5\CUETools.exe"""
  override def apply(cueFile: File, flacFile: File): IODirectory = {
    Process(CueToolsExe, Vector("/convert", s""""${cueFile.getAbsolutePath}"""")).!!
    IODirectory(IOFile(cueFile).parent / "convert")
  }
}
