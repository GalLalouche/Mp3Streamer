package songs.selector

import backend.recon.Track

import scala.util.Random

import common.Percentage
import common.rich.RichT.richT
import common.rich.primitives.RichDouble.richDouble

/**
 * Applies a filter to reduce the over-sampling of artists with many songs, i.e., giving artists
 * with fewer songs a better chance of passing this filter. For example, consider the case where one
 * artist has makes up half the playlist, with 10 artists making up the other half. Without scaling,
 * the artist would make up half the random songs.
 *
 * There are two major knobs for controlling this filter: value count (VC), and scaling factor (SF).
 * For a given artist, with VC=v and SF=s, their chance of passing filter is `N ^ -scalingFactor`.
 *
 * Effect of scaling factor:
 *   - If SF = 1, we get linear scaling (i.e., all artist will have equal representation, at least
 *     in terms of the value count.)
 *   - If SF = 0.5, we take the square root instead, if SF=0.75, it will be more pronounced, etc.
 *   - If SF = 0, this will be ignored.
 *
 * Choosing the right value count:
 *   - Using the number of songs is easiest, but consider the case where two artists have 10 hours
 *     of material, but one artist has 200 tracks (average 3 minutes per track) and one has 30
 *     (average of 20 minutes per track). Using number of songs means penalizing the former much
 *     more than the latter, even though both have roughly the same "amount of music".
 *   - Using the number of releases is a slightly less noisy alternative.
 *   - Using the total music length is probably the most "correct" option, but that requires parsing
 *     the ID3 values of all the songs, which is a lot slower than just counting files/directories.
 *
 * VC can in principle be [[None]] (edge cases and what not, e.g., classical music), in which case
 * this just returns `true`.
 */
private class SublinearScalingFilter(
    random: Random,
    scalingFactor: Percentage,
    artistValueCount: ArtistQuantifier,
) extends RandomFilterTemplate(random) {
  protected override def aux(track: Track): (Percentage, String) =
    artistValueCount(track.artist)
      .map { valueCount =>
        val result: Percentage = valueCount.requiring(_ > 0) ** (-scalingFactor.p)
        (result, s"${artistValueCount.simpleName}: $valueCount")
      }
      .getOrElse(1, "Defaults to passing since valueCount was None")
}
