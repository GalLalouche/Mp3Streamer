package songs

import models.IOSong

import common.rich.path.Directory
import common.rich.RichT._

private object SongGroupsUpdater {
  private def trackNumbers(
      directory: String,
      trackNumbersFirst: Int,
      trackNumbersSecond: Int,
      trackNumbersRest: Int*,
  ): SongGroup = {
    val trackNumbers = trackNumbersFirst :: trackNumbersSecond :: trackNumbersRest.toList
    val dir = Directory(directory)
    val prefixes: Set[String] = trackNumbers.map(_.toString.mapIf(_.length < 2).to("0" + _)).toSet
    def isPrefix(s: String) = prefixes.exists(s.startsWith)
    val songs = dir.files.filter(_.getName |> isPrefix)
    SongGroup(songs.sortBy(_.getName).map(IOSong.read))
  }

  // Appends new groups and saves them
  def main(args: Array[String]): Unit = {
    import backend.module.StandaloneModule
    import com.google.inject.Guice
    import models.ModelJsonable._
    import net.codingwell.scalaguice.InjectorExtensions._

    val injector = Guice.createInjector(StandaloneModule)
    val sg = injector.instance[SongGroups]
    def append(g: SongGroup): Unit = (g :: sg.load.toList).toSet |> sg.save
    val group: SongGroup =
      trackNumbers("""G:\Media\Music\Rock\Punk\Pistolita\2010 The Paper Boy""", 1, 2)
    append(group)
    println("Done")
  }
}
