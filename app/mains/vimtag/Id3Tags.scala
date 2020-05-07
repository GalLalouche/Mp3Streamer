package mains.vimtag

import java.io.File

private case class ParsedId3(
    artist: ParsedTag[String],
    album: ParsedTag[String],
    year: ParsedTag[Int],

    composer: ParsedTag[String],
    opus: ParsedTag[String],
    conductor: ParsedTag[String],
    orchestra: ParsedTag[String],
    performanceYear: ParsedTag[Int],

    flags: Set[Flag],

    songId3s: Seq[IndividualId3],
) {
  def files: Seq[File] = songId3s.map(_.file)
}

// Individual tags should never be keep since there's no point to it: just keep the existing value.
private case class IndividualId3(
    file: File,
    title: String,
    track: Int,
    discNumber: Option[String],
) {
  require(track > 0)
}
