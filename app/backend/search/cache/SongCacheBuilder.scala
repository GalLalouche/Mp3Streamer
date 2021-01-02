package backend.search.cache

import java.time.Clock

import backend.logging.LoggingLevel
import javax.inject.Inject
import models.MusicFinder
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

import common.concurrency.report.ReportObservable
import common.io.FileRef
import common.rich.RichTime.RichClock
import common.TimedLogger
import common.concurrency.report.ReportObservable.ReportObservable

private class SongCacheBuilder @Inject()(
    mf: MusicFinder,
    ec: ExecutionContext,
    clock: Clock,
    timedLogger: TimedLogger,
) {
  private implicit val iec: ExecutionContext = ec
  private def songFiles =
    timedLogger("fetching song files", LoggingLevel.Info)(Observable.from(mf.getSongFiles))
  private def extractSongMetadata(f: FileRef) = TimestampedSong(clock.getLocalDateTime, mf.parseSong(f))
  @deprecated("You should probably use TimestampedSongs.fromSongJsonFile")
  def fresh(): ReportObservable[TimestampedSong, SongCache] =
    ReportObservable.aggregator(songFiles.map(extractSongMetadata), SongCache.apply)
  def updating(cache: SongCache): ReportObservable[TimestampedSong, SongCache] =
    ReportObservable.filteringAggregator(
      observable = songFiles.map {f =>
        val needsUpdate = cache.needsUpdate(f)
        (needsUpdate, if (needsUpdate) extractSongMetadata(f) else cache.get(f).get)
      },
      finisher = SongCache.apply
    )
}
