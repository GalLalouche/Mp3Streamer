package songs

import common.rich.RichT._
import common.rich.path.Directory
import models.Song

private object SongGroupsUpdater {
  private def trackNumbers(directory: String, trackNumbersFirst: Int, trackNumbersSecond: Int, trackNumbersRest: Int*): SongGroup = {
    val trackNumbers = trackNumbersFirst :: trackNumbersSecond :: trackNumbersRest.toList
    val dir = Directory(directory)
    val prefixes: Set[String] = trackNumbers.map(_.toString.mapIf(_.length < 2).to("0" + _)).toSet
    def isPrefix(s: String) = prefixes exists s.startsWith
    val songs = dir.files.filter(_.getName |> isPrefix)
    SongGroup(songs.sortBy(_.getName).map(Song.apply))
  }

  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    import backend.configs.{Configuration, StandaloneConfig}
    import models.ModelJsonable._
    import net.codingwell.scalaguice.InjectorExtensions._

    val c: Configuration = StandaloneConfig
    val sg = c.injector.instance[SongGroups]
    def append(g: SongGroup): Unit = (g :: sg.load.toList).toSet |> sg.save
    val group: SongGroup = trackNumbers("""D:\Media\Music\Rock\Punk\Pistolita\2010 The Paper Boy""", 1, 2)
    append(group)
    println("Done")
  }
}
