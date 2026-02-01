package models

import common.path.ref.FileRef

trait SongTagParser {
  def apply(file: FileRef): Song
}
