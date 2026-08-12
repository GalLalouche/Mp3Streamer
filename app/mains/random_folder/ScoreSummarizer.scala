package mains.random_folder

import backend.score.{AggregateScorer, ModelScore}
import com.google.inject.Inject
import common.path.ref.FileRef
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.{Percentage, TimedLogger}
import scribe.Level

private class ScoreSummarizer @Inject() (scorer: AggregateScorer, timedLogger: TimedLogger) {
  def summary(songs: Iterable[FileRef]): Unit = timedLogger(s"Summarizing scores", Level.Debug) {
    val allScores = songs.flatMap(scorer.tryAggregateScore).flatMap(_.toModelScore).frequencies
    val totalSongs = allScores.values.sum
    ModelScore.values.foreach { score =>
      val p = Percentage(allScores.getOrElse(score, 0).toDouble / totalSongs)
      scribe.info(s"Score $score makes up ${p.prettyPrint(2)} of total playlist")
    }
  }
}
