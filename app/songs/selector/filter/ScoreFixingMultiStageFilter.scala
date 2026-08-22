package songs.selector.filter

import backend.recon.Track
import backend.score.{AggregateScorer, ModelScore}
import jakarta.inject.Inject
import models.Song
import rx.lang.scala.Observer
import songs.selector.filter.sublinear.SublinearScalingFilter

import scala.util.Random

import common.Filter
import common.path.ref.FileRef
import common.rich.RichRandom.richRandom

/**
 * This class exists to hand the probability discrepancy caused by having two different but not
 * independent random filters: score based and sublinear scaling. Since score based filtering has a
 * pre-set probability distribution, any additional filter applied after it can skew the
 * distribution if the additional filter is not independent of the score based filter. For example,
 * maybe artists with score A have a lot more songs than other artists, so applying a sublinear
 * scaling filter after the score based filter will skew the results to have fewer songs with score
 * A.
 *
 * The solution is simple: first we fix the score we want, and then apply all the filters. Since the
 * score is fixed, all the other filters cannot skew the score distribution.
 *
 * This can have one noticeable issue: if some score's actual distribution is very low, it may take
 * a lot of rolls to find a song with that score. This could be solved by having a reverse index
 * from score to songs, but we don't have one yet.
 */
private class ScoreFixingMultiStageFilter @Inject() (
    expectedScores: Map[ModelScore, Double],
    scorer: AggregateScorer,
    sublinearScaling: SublinearScalingFilter,
    lengthFilter: LengthFilter,
    observer: Observer[TrackFilterEvent],
    random: Random,
) extends MultiStageFilterFactory {
  private val scores: Seq[(Double, ModelScore)] = expectedScores.toVector.map(_.swap)
  override def next(): MultiStageFilter = {
    val scoreFilter = toFilter(random.selectW(scores))
    new MultiStageFilter {
      override def fileFilter: Filter[FileRef] = Filter.always
      override def trackFilter: Filter[Track] = scoreFilter && sublinearScaling
      override def songFilter: Filter[Song] = lengthFilter
    }
  }

  private def toFilter(selectedScore: ModelScore): Filter[Track] = { track =>
    val $ = scorer.aggregateScore(track).toModelScore.getOrElse(ModelScore.Okay) == selectedScore
    observer.onNext(TrackFilterEvent(FilterName, track, expectedScores(selectedScore), passed = $))
    $
  }
  private val FilterName = "ScoreFixingMultiStageFilter.score"
}
