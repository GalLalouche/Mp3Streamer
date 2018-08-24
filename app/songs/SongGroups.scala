package songs

import backend.configs.Configuration
import common.io.{DirectoryRef, FileRef, RootDirectory}
import common.json.{Jsonable, ToJsonableOps}
import common.rich.RichT._
import common.rich.func.MoreSeqInstances
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scalaz.syntax.ToFunctorOps

class SongGroups(implicit songJsonable: Jsonable[Song]) extends ToJsonableOps {
  private def getJsonFile(implicit c: Configuration): FileRef = {
    val rootDirectory = c.injector.instance[DirectoryRef, RootDirectory]
    rootDirectory addFile "song_groups.json"
  }
  private def writeToJsonFile(s: String)(implicit c: Configuration) =
    getJsonFile write s
  def save(groups: Traversable[SongGroup])(implicit c: Configuration): Unit = groups
      .map(_.songs.jsonify)
      .map(_.toString)
      .mkString("\n") |> writeToJsonFile
  def load(implicit c: Configuration): Set[SongGroup] = getJsonFile.lines
      .map(_.parseJsonable[Seq[Song]] |> SongGroup.apply)
      .toSet
}

object SongGroups extends MoreSeqInstances with ToFunctorOps {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())((agg, group) => agg ++ group.songs.fproduct(group.const))
}
