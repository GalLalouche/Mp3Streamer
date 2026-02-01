package mains.splitter

import java.io.File

import common.path.ref.io.IODirectory
import common.rich.RichFile._

private trait CueSplitter {
  def apply(cueFile: File, flacFile: File): IODirectory
  def apply(cueFile: File): IODirectory = apply(
    cueFile,
    flacFile = (cueFile.parent / (cueFile.nameWithoutExtension + ".flac")).asFile,
  )
}
