package songs.selector.filter

import backend.score.IndividualScorer
import com.google.inject.Provides
import genre.GenreFinder
import net.codingwell.scalaguice.ScalaModule
import rx.lang.scala.Observer
import songs.selector.filter.sublinear.SublinearScalingModule

import scala.concurrent.duration.DurationInt

import common.rx.RichObserver

private[selector] object FilterModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Observer[TrackFilterEvent]].toInstance(RichObserver.noop)
    bind[MultiStageFilterFactory].to[ScoreFixingMultiStageFilter]
    install(SublinearScalingModule)
  }

  @Provides private def lengthFilter(
      genreFinder: GenreFinder,
      scorer: IndividualScorer,
  ) = new LengthFilter(genreFinder, scorer, minLength = 2.minutes)
}
