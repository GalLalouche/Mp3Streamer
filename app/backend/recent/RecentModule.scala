package backend.recent

import javax.inject.Singleton

import com.google.inject.Provides
import models.{AlbumDir, AlbumFactory}
import net.codingwell.scalaguice.ScalaPrivateModule
import rx.lang.scala.{Observable, Observer}

import common.PipeSubject
import common.io.DirectoryRef

object RecentModule extends ScalaPrivateModule {
  private type DirToAlbum = PipeSubject[DirectoryRef, AlbumDir]
  val WebSocketName = "Recent"
  override def configure(): Unit = {
    bind[Observer[DirectoryRef]].annotatedWith[NewDir].to[DirToAlbum]
    bind[Observable[AlbumDir]].annotatedWith[NewDir].to[DirToAlbum]
    expose[Observer[DirectoryRef]].annotatedWith[NewDir]
    expose[Observable[AlbumDir]].annotatedWith[NewDir]
  }

  @Provides
  @Singleton
  private def subject(albumFactory: AlbumFactory): DirToAlbum = PipeSubject(albumFactory.fromDir)
}
