package songs

import backend.configs.StandaloneConfig
import common.rich.RichT._
import common.rich.path.Directory
import models.Song

private object SongGroupsUpdater {
  private def trackNumbers(directory: String, trackNumbersFirst: Int, trackNumbersSecond: Int, trackNumbersRest: Int*): SongGroup = {
    val trackNumbers = trackNumbersFirst :: trackNumbersSecond :: trackNumbersRest.toList
    val dir = Directory(directory)
    val prefixes: Set[String] = trackNumbers.map(_.toString.mapIf(_.length < 2).to("0".+)).toSet
    def isPrefix(s: String) = prefixes exists s.startsWith
    val songs = dir.files.filter(_.getName |> isPrefix)
    SongGroup(songs.sortBy(_.getName).map(Song.apply))
  }
  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    implicit val c = StandaloneConfig
    import c._
    import backend.search.ModelJsonable._
    val sg = new SongGroups
    def append(g: SongGroup) = (g :: sg.load.toList).toSet |> sg.save
    val group: SongGroup = trackNumbers("""D:\Media\Music\Rock\Punk\Pistolita\2010 The Paper Boy""", 1, 2)
    append(group)
    println("Done")
  }
}
