package mains.random_folder

import backend.recon.Reconcilable.SongExtractor
import backend.score.model.{ModelScore, SourcedOptionalModelScore}
import backend.score.scorer.AggregateScorer
import com.google.inject.Inject
import models.SongTagParser
import scribe.Level

import cats.implicits.catsSyntaxOptionId

import common.{Percentage, TimedLogger}
import common.path.ref.FileRef
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce

class ScoreSummarizer @Inject() (
    scorer: AggregateScorer,
    timedLogger: TimedLogger,
    songTagParser: SongTagParser,
) {
  def summary(songs: Iterable[FileRef], force: Boolean = false): Seq[Double] =
    timedLogger(s"Summarizing scores", Level.Debug) {
      def score(f: FileRef): Option[SourcedOptionalModelScore] = {
        val res = scorer.tryAggregateScore(f)
        res.mapIf(force).to(_.getOrElse(scorer.aggregateScore(songTagParser(f).track)).some)
      }

      val allScores = songs.flatMap(score).flatMap(_.toModelScore).frequencies
      println("Summarizing a total of " + allScores.values.sum + " songs")
      val totalSongs = allScores.values.sum
      ModelScore.values.foreach { score =>
        val p = Percentage(allScores.getOrElse(score, 0).toDouble / totalSongs)
        scribe.info(s"Score $score makes up ${p.prettyPrint(2)} of total playlist")
      }
      ModelScore.values.map(score => allScores.getOrElse(score, 0).toDouble / totalSongs).toVector
    }
}
