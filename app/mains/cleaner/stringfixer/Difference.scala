package mains.cleaner.stringfixer

import models.Song

private case class Difference(song: Song, field: String, actual: String, expected: String) {
  def formattedString =
    s"""${song.file} @ $field\n
      |\tactual  : $actual
      |\texpected: $expected""".stripMargin
}
