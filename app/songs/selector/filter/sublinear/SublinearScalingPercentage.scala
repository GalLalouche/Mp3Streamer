package songs.selector.filter.sublinear

import backend.recon.Track
import com.google.common.annotations.VisibleForTesting
import com.google.inject.name.Named
import jakarta.inject.Inject
import songs.selector.filter.sublinear.SublinearScalingPercentage.Result

import common.Percentage
import common.rich.RichT.richT
import common.rich.primitives.RichDouble.richDouble

/**
 * Applies a filter to reduce the over-sampling of artists with many songs, i.e., giving artists
 * with fewer songs a better chance of passing this filter. For example, consider the case where one
 * artist has makes up half the playlist, with 10 artists making up the other half. Without scaling,
 * the artist would make up half the random songs.
 *
 * There are three major knobs for controlling this filter: value count (VC), scaling factor (SF),
 * and damping factor (DF).
 *
 * The actual formula is `f(VC) = (VC ^ (1 - SF) + DF) / (VC + DF)`. It was chosen because it is
 * monotonic, between 0 and 1, 1 when SF is 0, f(1) = 1, converges to VC ^ (1 - SF) and enables the
 * aforementioned knobs.
 *
 * Effect of scaling factor:
 *   - If SF = 1, we get linear scaling (i.e., all artist will have equal representation, at least
 *     in terms of the value count.)
 *   - If SF = 0.5, we get sqrt(vc) (up to damping)
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
 *
 * The damping factor reduces the effect the scaling has on smaller values. Should be a positive
 * number.
 */
private class SublinearScalingPercentage @Inject() (
    artistValueCount: ArtistQuantifier,
    @Named(SublinearScalingPercentage.ScalingFactor) scalingFactor: Percentage,
    @Named(SublinearScalingPercentage.DampingFactor) dampingFactor: Double,
) {
  def apply(track: Track): Result =
    if (scalingFactor.p == 0)
      constantPass("Scaling factor is 0, so this filter is ignored")
    else
      artistValueCount(track.artist)
        .map { valueCount =>
          require(valueCount > 0, s"Value count for <$track> was negative <$valueCount>")
          Result(formula(valueCount), s"${artistValueCount.simpleName}: $valueCount")
        }
        .getOrElse(constantPass("Defaults to passing since valueCount was None"))

  private def constantPass(description: String) = Result(1, description)

  private val exp = scalingFactor.inverse.p
  private def formula(x: Int): Percentage =
    SublinearScalingPercentage.formula(x, exp, dampingFactor)
}

private[selector] object SublinearScalingPercentage {
  final val ScalingFactor = "SublinearScalingFilter.scalingFactor"
  final val DampingFactor = "SublinearScalingFilter.dampingFactor"

  @VisibleForTesting private[sublinear] def formula(x: Int, exp: Double, c: Double): Percentage =
    ((x.toDouble ** exp) + c) / (x + c)

  case class Result(chanceToPass: Percentage, description: String)
}
