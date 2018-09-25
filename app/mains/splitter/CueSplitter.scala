package mains.splitter

import java.io.File

import common.rich.path.Directory

private trait CueSplitter {
  def apply(cueFile: File, flacFile: File): Directory
  def apply(cueFile: File): Directory =
    apply(cueFile, flacFile = new File(cueFile.getAbsolutePath.takeWhile(_ != '.') + ".flac"))
}
