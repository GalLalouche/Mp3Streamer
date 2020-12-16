package backend.recent

import com.google.inject.Provides
import javax.inject.Singleton
import models.{Album, AlbumFactory}
import net.codingwell.scalaguice.ScalaPrivateModule
import rx.lang.scala.{Observable, Observer}

import common.io.DirectoryRef
import common.PipeSubject

object RecentModule extends ScalaPrivateModule {
  private type DirToAlbum = PipeSubject[DirectoryRef, Album]
  val WebSocketName = "Recent"
  override def configure(): Unit = {
    bind[Observer[DirectoryRef]].annotatedWith[NewDir].to[DirToAlbum]
    bind[Observable[Album]].annotatedWith[NewDir].to[DirToAlbum]
    expose[Observer[DirectoryRef]].annotatedWith[NewDir]
    expose[Observable[Album]].annotatedWith[NewDir]
  }

  @Provides
  @Singleton
  private def subject(albumFactory: AlbumFactory): DirToAlbum = PipeSubject(albumFactory.fromDir)
}
