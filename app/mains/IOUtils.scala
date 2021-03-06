package mains

import java.io.File

import common.rich.primitives.RichString._

private object IOUtils {
  /** Opens windows explorer with the file in focus */
  def focus(f: File): Unit =
    Runtime.getRuntime.exec(
      s"""explorer.exe /select,"${f.getAbsolutePath.simpleReplace("/", "\\")}"""")
}
