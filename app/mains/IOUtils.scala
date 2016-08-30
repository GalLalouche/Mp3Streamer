package mains

import java.io.File

private[mains] object IOUtils {
  /** Opens windows explorer with the file in focus */
  def focus(f: File) {
    Runtime.getRuntime.exec("explorer.exe /select," + f.getAbsolutePath)
  }
}