package mains

import backend.configs.{CleanConfiguration, Configuration}
import common.io.IODirectory
import common.rich.collections.RichTraversableOnce._
import common.rich.path.{Directory, RichFileUtils}

private object PrefixAlbumDirsWithYears {
  private implicit val c: Configuration = CleanConfiguration
  private def addYear(d: Directory): Unit = {
    val songs = c.mf getSongsInDir IODirectory(d)
    val year = songs.map(_.year).toSet.single
    RichFileUtils.rename(d, s"$year ${d.name}")
  }
  private def hasYear(d: Directory): Boolean = d.name.matches("^\\d{4}.*$")

  def main(args: Array[String]): Unit = {
    val dir = Directory("""D:\Incoming\Bittorrent\Completed\Music\A Silver Mt Zion""")
    dir.dirs filterNot hasYear foreach addYear
  }
}
