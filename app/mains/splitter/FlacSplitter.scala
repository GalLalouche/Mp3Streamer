package mains.splitter

import java.io.File

//splits cue file and fixes the flac output
private object FlacSplitter {
  def main(args: Array[String]) {
    CueTools split new File(args(0))
  }
}
