package mains.splitter

import java.io.File
import javax.inject.Inject
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

import scala.collection.JavaConverters._

import common.io.IODirectory
import common.rich.collections.RichIterable._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.{Directory, RichFileUtils}
import common.rich.path.RichFile._
import musicfinder.IOMusicFinder

// Splits cue file and fixes the flac output.
private class FlacSplitter @Inject() (cueSplitter: CueSplitter, mf: IOMusicFinder) {
  private def clean(output: Directory, destination: Directory, bigFlac: File): Unit = {
    println("Moving flac files to parent dir")
    output.files.find(_.name == "00. (HTOA).flac").foreach(_.delete())
    output.files.find(_.name == "garbage.cue").foreach(_.delete())
    output.files.filter(_.extension == "flac").foreach(f => RichFileUtils.move(f, destination))
    println("Deleting convert dir")
    output.deleteAll()

    // Append year manually if it doesn't exist, since for some reason the splitters won't
    lazy val bigFlacAudioFile = AudioFileIO.read(bigFlac)

    bigFlacAudioFile.getTag
      .getFields(FieldKey.YEAR)
      .asScala
      .headOption
      .foreach(year =>
        mf.getSongFilesInDir(IODirectory(destination)).foreach { f =>
          val audioFile = AudioFileIO.read(f.file)
          if (audioFile.getTag.getFields(FieldKey.YEAR).isEmpty) {
            println("Fixing year on " + f.name)
            audioFile.getTag.setField(year)
            audioFile.commit()
          }
        },
      )
  }

  def apply(cueFile: File): Unit = {
    val dir = cueFile.parent
    val bigFlacFile = {
      val $ = dir.files.filter(_.extension == "flac")
      if ($.hasExactlySizeOf(1))
        $.single
      else
        $.filter(_.nameWithoutExtension == cueFile.nameWithoutExtension).single
    }
    val output = cueSplitter(cueFile)
    clean(output, dir, bigFlacFile)
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
