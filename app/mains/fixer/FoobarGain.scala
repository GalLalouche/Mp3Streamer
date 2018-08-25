package mains.fixer

import backend.configs.RealConfig
import common.io.IODirectory
import common.rich.path.Directory
import models.IOMusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

import scala.sys.process._

private object FoobarGain {
  private val foobarPath = """C:\Program Files (x86)\foobar2000\foobar2000.exe"""
  private val replayGainCommand = "/context_command:\"ReplayGain/Scan per-file track gain\""

  /**
   * Calculates the track gain of the files in the directory. Since this uses Foobar2000, there's no
   * way to verify that the task completed. Therefore, just run this last and hope for the best :|
   */
  def calculateTrackGain(d: Directory)(implicit c: RealConfig): Unit = {
    // SBT does not support both string interpolation and quote marks :\
    val mf = c.injector.instance[IOMusicFinder]
    val fileNames = mf.getSongFilesInDir(IODirectory(d)).map("\"" + _.path + "\"").mkString(" ")
    s"$foobarPath $replayGainCommand $fileNames".run()
  }
}
