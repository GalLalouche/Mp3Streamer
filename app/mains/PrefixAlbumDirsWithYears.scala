package mains

import backend.configs.{CleanConfiguration, Configuration}
import common.io.IODirectory
import common.rich.collections.RichTraversableOnce._
import common.rich.path.{Directory, RichFileUtils}
import models.MusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

private object PrefixAlbumDirsWithYears {
  private implicit val c: Configuration = CleanConfiguration
  val mf = c.injector.instance[MusicFinder]
  private def addYear(d: Directory): Unit = {
    try {
      val songs = mf getSongsInDir IODirectory(d)
      val year = songs.map(_.year).toSet.single
      RichFileUtils.rename(d, s"$year ${d.name}")
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        println("Error renaming " + d)
    }
  }
  private def hasYear(d: Directory): Boolean = d.name.matches("^\\d{4}.*$")

  def main(args: Array[String]): Unit = {
    val dir = Directory("""E:\Incoming\Bittorrent\Completed\Music\B&S CD's\Albums""")
    dir.dirs filterNot hasYear foreach addYear
  }
}
