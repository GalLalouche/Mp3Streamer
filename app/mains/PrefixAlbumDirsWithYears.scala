package mains

import com.google.inject.Inject
import musicfinder.SongDirectoryParser

import common.io.IODirectory
import common.rich.collections.RichTraversableOnce._
import common.rich.path.{Directory, RichFileUtils}

private class PrefixAlbumDirsWithYears @Inject() (songDirectoryParser: SongDirectoryParser) {
  def addYears(d: Directory): Unit = d.dirs.filterNot(hasYear).foreach(addYear)

  private def addYear(d: Directory): Unit = try {
    val songs = songDirectoryParser(IODirectory(d))
    val year = songs.map(_.year).toSet.single
    RichFileUtils.rename(d, s"$year ${d.name}")
  } catch {
    case e: Throwable =>
      e.printStackTrace()
      println("Error renaming " + d)
  }
  private def hasYear(d: Directory): Boolean = d.name.take(4).forall(_.isDigit)
}
