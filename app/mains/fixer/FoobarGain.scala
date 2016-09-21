package mains.fixer

import common.io.IODirectory
import common.rich.path.Directory
import models.MusicFinder

import scala.sys.process._


object FoobarGain {
  private val foobarPath = """C:\Program Files (x86)\foobar2000\foobar2000.exe"""
  private val replayGainCommand = "/context_command:\"ReplayGain/Scan per-file track gain\""

  /**
   * Calculates the track gain of the files in the directory. Since this uses Foobar2000, there's no
   * way to verify that the task completed. Therefore, just run this last and hope for the best :|
   */
  def calculateTrackGain(d: Directory)(implicit mf: MusicFinder): Unit = {
    import common.rich.RichT._
    // SBT does not support both string interpolation and quote marks :\
    val fileNames = mf.getSongFilePathsInDir(IODirectory(d)).map("\"" + _ + "\"").mkString(" ")
    s"$foobarPath $replayGainCommand $fileNames".log().!!
  }
}
