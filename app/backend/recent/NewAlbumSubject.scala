package backend.recent

import common.io.DirectoryRef
import javax.inject.{Inject, Singleton}
import models.{Album, AlbumFactory}
import rx.lang.scala.{Observable, Observer, Subject}

// TODO extract (in ScalaCommon) general concept: given a subject of A, and a map from A to B, expose an observer of A and an observable of B
@Singleton
private class NewAlbumSubject @Inject()(albumFactory: AlbumFactory) {
  private val dirRefSubject = Subject[DirectoryRef]()
  def observer: Observer[DirectoryRef] = dirRefSubject
  def observable: Observable[Album] = dirRefSubject.map(albumFactory.fromDir)
}
