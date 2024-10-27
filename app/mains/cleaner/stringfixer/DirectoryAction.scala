package mains.cleaner.stringfixer

import common.io.IODirectory

private trait DirectoryAction {
  def go(): Unit
  def directory: IODirectory
}
