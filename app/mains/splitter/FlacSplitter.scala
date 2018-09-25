package mains.splitter

import java.io.File

import common.rich.path.{Directory, RichFileUtils}
import common.rich.path.RichFile._
import javax.inject.Inject

//splits cue file and fixes the flac output
private class FlacSplitter @Inject()(cueSplitter: CueSplitter) {
  private def clean(output: Directory, destination: Directory): Unit = {
    println("Moving flac files to parent dir")
    output.files.find(_.name == "00. (HTOA).flac").foreach(_.delete())
    output.files.find(_.name == "garbage.cue").foreach(_.delete())
    output.files.filter(_.extension == "flac").foreach(f => RichFileUtils.move(f, destination))
    println("Deleting convert dir")
    output.deleteAll()
  }

  def apply(file: File): Unit = {
    val dir = cueSplitter(file)
    clean(dir, file.parent)
    println("Done")
  }
}

private object FlacSplitter {
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(StandaloneModule, SplitterModule)
    val $ = injector.instance[FlacSplitter]
    $(new File(args(0)))
  }
}
