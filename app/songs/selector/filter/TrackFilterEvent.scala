package songs.selector.filter

import backend.recon.Track

import common.Percentage

private[selector] case class TrackFilterEvent(
    filterName: String,
    track: Track,
    percentage: Percentage,
    passed: Boolean,
)
