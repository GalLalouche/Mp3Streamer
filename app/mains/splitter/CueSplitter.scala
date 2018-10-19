package mains.splitter

import java.io.File

import common.rich.path.Directory
import common.rich.path.RichFile._

private trait CueSplitter {
  def apply(cueFile: File, flacFile: File): Directory
  def apply(cueFile: File): Directory =
    apply(cueFile, flacFile = cueFile.parent / (cueFile.nameWithoutExtension + ".flac"))
}
