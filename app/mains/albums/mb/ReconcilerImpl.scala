package mains.albums.mb

import mains.albums.Reconciler
import models.RealLocations

import scala.concurrent.ExecutionContext.Implicits.global

object ReconcilerImpl extends Reconciler(ReconStorageImpl, MusicBrainzRetriever) {
  def main(args: Array[String]) {
    fill(new RealLocations { override val subDirs = List("Rock", "Metal") })
  }
}
