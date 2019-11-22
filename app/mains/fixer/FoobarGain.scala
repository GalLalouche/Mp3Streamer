package mains.fixer

import javax.inject.Inject
import models.IOMusicFinder

import scala.sys.process._

import common.io.IODirectory
import common.rich.path.Directory
import common.rich.primitives.RichString._

private class FoobarGain @Inject()(mf: IOMusicFinder) {
  private val foobarPath = """C:\Program Files (x86)\foobar2000\foobar2000.exe"""
  private val replayGainCommand = """/context_command:"ReplayGain/Scan per-file track gain""""

  /**
   * Calculates the track gain of the files in the directory. Since this uses Foobar2000, there's no
   * way to verify that the task completed. Therefore, just run this last and hope for the best :|
   */
  def apply(d: Directory): Unit = {
    val fileNames = mf.getSongFilesInDir(IODirectory(d)).map(_.path.quote).mkString(" ")
    s"$foobarPath $replayGainCommand $fileNames".run()
  }
}
