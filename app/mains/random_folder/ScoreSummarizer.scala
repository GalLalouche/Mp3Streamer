package mains.random_folder

import javax.inject.Inject

import backend.logging.FilteringLogger
import backend.scorer.{CachedModelScorer, ModelScore}
import models.MusicFinder

import common.Percentage
import common.io.IODirectory
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class ScoreSummarizer @Inject() (
    mf: MusicFinder,
    scorer: CachedModelScorer,
    logger: FilteringLogger,
) {
  def summary(
      outputDir: IODirectory,
      totalSongs: Int,
  ): Unit = {
    val allScores =
      mf.getSongFilesInDir(outputDir)
        .view
        .map(mf.parseSong)
        .flatMap(scorer(_).toModelScore)
        .frequencies
    ModelScore.values.foreach(score =>
      logger.info(
        s"Score $score makes up " +
          s"${Percentage(allScores.getOrElse(score, 0).toDouble / totalSongs).prettyPrint(2)} of total playlist",
      ),
    )
  }
}
