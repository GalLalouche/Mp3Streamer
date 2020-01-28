package mains.splitter

import java.io.File

import common.rich.path.RichFile._

private object Mp3SpltSplitter extends CueSplitter {
  private val Mp3SpltPath = new File("""C:\Program Files (x86)\mp3splt""")

  override def apply(cueFile: File, flacFile: File) = {
    val output = cueFile.parent.addSubDir("convert").clear()
    sys.process.Process(
      Vector("""C:\Program Files (x86)\mp3splt\mp3splt.exe""", "-c", cueFile.getAbsolutePath, "-d", output.path, flacFile.path),
      Mp3SpltPath
    ).!
    output
  }
}

