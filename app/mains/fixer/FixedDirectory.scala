package mains.fixer

import common.rich.path.{Directory, RichFileUtils}

private class FixedDirectory(val dir: Directory, val name: String) {
  def move(to: Directory): Directory = RichFileUtils.move(dir, to, name)
}
