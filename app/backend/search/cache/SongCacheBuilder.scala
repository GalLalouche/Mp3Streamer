package backend.search.cache

import java.time.Clock

import com.google.inject.Inject
import models.SongTagParser
import musicfinder.MusicFiles

import common.io.FileRef
import common.rich.RichT.richT
import common.rich.RichTime.RichClock
import common.rich.primitives.RichBoolean.richBoolean
import common.rx.report.ReportObservable
import common.rx.report.ReportObservable.ReportObservable

private class SongCacheBuilder @Inject() (
    mf: MusicFiles,
    songTagParser: SongTagParser,
    clock: Clock,
) {
  def updating(sc: SongCache): ReportObservable[TimestampedSong, SongCache] =
    ReportObservable.filteringAggregator(
      observable = mf.getSongFiles.map(f =>
        sc.needsUpdate(f) :-> (_.fold(extractSongMetadata(f), sc.get(f).get)),
      ),
      finisher = SongCache.from,
    )

  private def extractSongMetadata(f: FileRef) =
    TimestampedSong(clock.getLocalDateTime, songTagParser(f))
}
