package mains.vimtag

import models.Album.AlbumTitle
import models.Song.{SongTitle, TrackNumber}

private case class ParsedId3(
    artist: ParsedTag[String],
    album: ParsedTag[AlbumTitle],
    year: ParsedTag[Int],
    composer: ParsedTag[String],
    opus: ParsedTag[String],
    conductor: ParsedTag[String],
    orchestra: ParsedTag[String],
    performanceYear: ParsedTag[Int],
    flags: Set[Flag],
    songId3s: Seq[IndividualId3],
) {
  def files: Seq[String] = songId3s.map(_.relativeFileName)
}

// Individual tags should never be Keep since there's no point to it: just keep the existing value.
private case class IndividualId3(
    relativeFileName: String,
    title: SongTitle,
    track: TrackNumber,
    discNumber: Option[String],
) {
  require(track > 0)
}

private object IndividualId3 {
  def apply(
      relativeFileName: String,
      title: SongTitle,
      track: TrackNumber,
      discNumber: Option[String],
  ): IndividualId3 = new IndividualId3(
    relativeFileName = relativeFileName.trim,
    title = title.trim,
    track = track,
    discNumber = discNumber.map(_.trim),
  )
}
