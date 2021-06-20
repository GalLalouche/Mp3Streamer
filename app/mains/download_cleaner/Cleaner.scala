package mains.download_cleaner

import common.io.IODirectory

private trait Cleaner {
  def apply(dir: IODirectory): Unit
}
