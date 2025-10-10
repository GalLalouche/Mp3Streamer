package backend.search.cache

import java.time.Clock

import com.google.inject.Inject
import models.SongTagParser
import musicfinder.MusicFinder
import rx.lang.scala.Observable

import common.TimedLogger
import common.concurrency.report.ReportObservable
import common.concurrency.report.ReportObservable.ReportObservable
import common.io.FileRef
import common.rich.RichT.richT
import common.rich.RichTime.RichClock
import common.rich.primitives.RichBoolean.richBoolean

private class SongCacheBuilder @Inject() (
    mf: MusicFinder,
    songTagParser: SongTagParser,
    clock: Clock,
    timedLogger: TimedLogger,
) {
  def updating(sc: SongCache): ReportObservable[TimestampedSong, SongCache] =
    ReportObservable.filteringAggregator(
      observable = {
        val songFiles = timedLogger("fetching song files", scribe.info(_))(
          Observable.from(mf.getSongFiles.toVector),
        )
        songFiles.map(f => sc.needsUpdate(f) :-> (_.fold(extractSongMetadata(f), sc.get(f).get)))
      },
      finisher = SongCache.from,
    )

  private def extractSongMetadata(f: FileRef) =
    TimestampedSong(clock.getLocalDateTime, songTagParser(f))
}
