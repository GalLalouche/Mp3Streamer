package backend.search.cache

import java.time.Clock
import javax.inject.Inject
import rx.lang.scala.Observable

import common.TimedLogger
import common.concurrency.report.ReportObservable
import common.concurrency.report.ReportObservable.ReportObservable
import common.io.FileRef
import common.rich.RichTime.RichClock
import musicfinder.MusicFinder

private class SongCacheBuilder @Inject() (
    mf: MusicFinder,
    clock: Clock,
    timedLogger: TimedLogger,
) {
  def updating(cache: SongCache): ReportObservable[TimestampedSong, SongCache] =
    ReportObservable.filteringAggregator(
      observable = {
        val songFiles = timedLogger("fetching song files", scribe.info(_))(
          Observable.from(mf.getSongFiles.toVector),
        )
        songFiles.map { f =>
          val needsUpdate = cache.needsUpdate(f)
          (needsUpdate, if (needsUpdate) extractSongMetadata(f) else cache.get(f).get)
        }
      },
      finisher = SongCache.apply,
    )

  private def extractSongMetadata(f: FileRef) =
    TimestampedSong(clock.getLocalDateTime, mf.parseSong(f))
}
