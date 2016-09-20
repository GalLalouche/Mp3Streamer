package mains.fixer

import common.rich.path.Directory

import scala.sys.process._


private object FoobarGain {
  private val foobarPath = """C:\Program Files (x86)\foobar2000\foobar2000.exe"""
  private val replayGainCommand = "/context_command:\"ReplayGain/Scan per-file track gain\""

  /**
   * Calculates the track gain of the files in the directory. Since this uses Foobar2000, there's no
   * way to verify that the task completed. Therefore, just run this last and hope for the best :|
   */
  def calculateTrackGain(d: Directory): Unit = {
    // SBT does not support both string interpolation and quote marks :\
    "%s %s \"%s\"".format(foobarPath, replayGainCommand, d.getAbsolutePath).!!
  }
}
