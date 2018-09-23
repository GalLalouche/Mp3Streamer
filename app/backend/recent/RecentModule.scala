package backend.recent

import com.google.inject.Provides
import common.io.DirectoryRef
import common.PipeSubject
import javax.inject.Singleton
import models.{Album, AlbumFactory}
import net.codingwell.scalaguice.ScalaPrivateModule
import rx.lang.scala.{Observable, Observer}

object RecentModule extends ScalaPrivateModule {
  private type DirToAlbum = PipeSubject[DirectoryRef, Album]
  override def configure(): Unit = {
    bind[Observer[DirectoryRef]].annotatedWith[NewDir].to[DirToAlbum]
    bind[Observable[Album]].annotatedWith[NewDir].to[DirToAlbum]
    expose[Observer[DirectoryRef]].annotatedWith[NewDir]
    expose[Observable[Album]].annotatedWith[NewDir]
  }

  @Provides
  @Singleton
  private def provideSubject(albumFactory: AlbumFactory): DirToAlbum = PipeSubject(albumFactory.fromDir)
}
