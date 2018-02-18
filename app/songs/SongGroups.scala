package songs

import common.Jsonable
import common.io.{FileRef, RootDirectoryProvider}
import common.rich.RichT._
import common.rich.func.MoreSeqInstances
import models.Song
import play.api.libs.json.Format

import scala.concurrent.ExecutionContext
import scalaz.syntax.ToFunctorOps

class SongGroups(implicit songJsonable: Format[Song]) extends Jsonable.ToJsonableOps {
  private def getJsonFile(implicit rootDirectoryProvider: RootDirectoryProvider, ec: ExecutionContext): FileRef =
    rootDirectoryProvider.rootDirectory addFile "song_groups.json"
  private def writeToJsonFile(s: String)(
      implicit rootDirectoryProvider: RootDirectoryProvider, ec: ExecutionContext) =
    getJsonFile write s
  def save(groups: Traversable[SongGroup])(
      implicit rootDirectoryProvider: RootDirectoryProvider, ec: ExecutionContext): Unit =
    groups
        .map(_.songs.jsonify)
        .map(_.toString)
        .mkString("\n") |> writeToJsonFile
  def load(implicit rootDirectoryProvider: RootDirectoryProvider, ec: ExecutionContext): Set[SongGroup] =
    getJsonFile.lines
        .map(_.parseJsonable[Seq[Song]] |> SongGroup)
        .toSet
}

object SongGroups extends MoreSeqInstances with ToFunctorOps {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())((agg, group) => agg ++ group.songs.fproduct(group.const))
}
