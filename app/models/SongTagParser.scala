package models

import common.io.FileRef

trait SongTagParser {
  def apply(file: FileRef): Song
}
