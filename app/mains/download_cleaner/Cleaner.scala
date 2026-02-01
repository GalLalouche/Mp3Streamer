package mains.download_cleaner

import common.path.ref.io.IODirectory

private trait Cleaner {
  def apply(dir: IODirectory): Unit
}
