package mains.fixer

import common.rich.path.Directory

private object FolderFixerDebugger {
  def main(args: Array[String]): Unit = {
    val dir = Directory("""E:\Incoming\Bittorrent\Completed\Music\Soen  - Lykaia (Limited Edition)  2017""")
    FolderFixer.main(Array[String](dir.path))
  }
}
