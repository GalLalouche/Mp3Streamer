package songs.selector.filter.sublinear

import backend.recon.Track
import jakarta.inject.Inject
import rx.lang.scala.Observer
import songs.selector.filter.TrackFilterEvent
import songs.selector.filter.sublinear.SublinearScalingPercentage.Result

import scala.util.Random

import common.Filter
import common.rich.RichT.richT

private[filter] class SublinearScalingFilter @Inject() (
    random: Random,
    sublinearScalingPercentage: SublinearScalingPercentage,
    observer: Observer[TrackFilterEvent],
) extends Filter[Track] {
  def passes(track: Track): Boolean = {
    val Result(passChance, description) = sublinearScalingPercentage(track)
    val shortSongString = s"${track.album.artist} - ${track.title} ($description)"
    val $ = passChance.roll(random)
    if ($) scribe.trace(s"Chose song <$shortSongString> with probability $passChance")
    else scribe.trace(s"Skipped song <$shortSongString> with probability ${passChance.inverse}")
    observer.onNext(TrackFilterEvent(this.simpleName, track, passChance, passed = $))
    $
  }
}
