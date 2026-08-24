package songs.selector

import backend.recon.Track

import scala.util.Random

import common.{Filter, Percentage}

private abstract class RandomFilterTemplate(random: Random) extends Filter[Track] {
  final override def passes(track: Track): Boolean = {
    val (percentage, description) = aux(track)
    val shortSongString = s"${track.album.artist} - ${track.title} ($description)"
    val $ = percentage.roll(random)
    if ($) scribe.trace(s"Chose song <$shortSongString> with probability $percentage")
    else scribe.trace(s"Skipped song <$shortSongString> with probability ${percentage.inverse}")
    $
  }
  protected def aux(track: Track): (Percentage, String)
}
