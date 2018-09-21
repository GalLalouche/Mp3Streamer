package backend.recent

import com.google.inject.Provides
import common.io.DirectoryRef
import models.Album
import net.codingwell.scalaguice.ScalaModule
import rx.lang.scala.{Observable, Observer}

object RecentModule extends ScalaModule {
  @Provides
  @NewDir
  private def provideObserver(recentSubject: NewAlbumSubject): Observer[DirectoryRef] = recentSubject.observer

  @Provides
  @NewDir
  private def provideObservable(recentSubject: NewAlbumSubject): Observable[Album] = recentSubject.observable
}
