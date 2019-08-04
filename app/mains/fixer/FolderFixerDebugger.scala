package mains.fixer

import common.rich.path.Directory

private object FolderFixerDebugger {
  def main(args: Array[String]): Unit = {
    val dir = Directory("""D:\Media\Music\Metal\Melodeath\Amon Amarth\2019 Berserker""")
    FolderFixer.main(Array[String](dir.path))
  }
}
