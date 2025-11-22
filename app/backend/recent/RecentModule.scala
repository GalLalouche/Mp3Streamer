package backend.recent

import com.google.inject.{Provides, Singleton}
import models.{AlbumDir, AlbumDirFactory}
import net.codingwell.scalaguice.ScalaPrivateModule
import rx.lang.scala.{Observable, Observer}

import common.PipeSubject
import common.guice.PrivateModuleUtils
import common.io.DirectoryRef

object RecentModule extends ScalaPrivateModule with PrivateModuleUtils {
  private type DirToAlbum = PipeSubject[DirectoryRef, AlbumDir]
  override def configure(): Unit = {
    bind[Observer[DirectoryRef]].annotatedWith[NewDir].to[DirToAlbum]
    bind[Observable[AlbumDir]].annotatedWith[NewDir].to[DirToAlbum]
    expose[Observer[DirectoryRef]].annotatedWith[NewDir]()
    expose[Observable[AlbumDir]].annotatedWith[NewDir]()
    publicBind[LastAlbumProvider].to[RecentAlbums]
  }

  @Provides
  @Singleton
  private def subject(albumFactory: AlbumDirFactory): DirToAlbum = PipeSubject(albumFactory.fromDir)
}
