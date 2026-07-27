package mains

import backend.recon.ReconcilableFactory
import com.google.inject.Inject
import musicfinder.SongDirectoryParser

import common.path.PathUtils
import common.path.ref.io.IODirectory
import common.rich.collections.RichTraversableOnce._

private class PrefixAlbumDirsWithYears @Inject() (songDirectoryParser: SongDirectoryParser) {
  def addYears(d: IODirectory): Unit =
    d.dirs.filterNot(ReconcilableFactory.hasYearPrefix).foreach(addYear)

  private def addYear(d: IODirectory): Unit = try {
    val songs = songDirectoryParser(d)
    val year = songs.map(_.year).toSet.single
    PathUtils.rename(d, s"$year ${d.name}")
  } catch {
    case e: Throwable =>
      e.printStackTrace()
      println("Error renaming " + d)
  }
}
