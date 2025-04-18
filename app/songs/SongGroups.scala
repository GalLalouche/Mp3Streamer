package songs

import com.google.inject.Inject

import models.Song

import common.rich.func.MoreSeqInstances._
import scalaz.syntax.functor.ToFunctorOps

import common.io.{DirectoryRef, RootDirectory}
import common.json.Jsonable
import common.json.ToJsonableOps._
import common.rich.RichT._

private class SongGroups @Inject() (@RootDirectory rootDirectory: DirectoryRef) {
  private lazy val jsonFile = rootDirectory.addFile("song_groups.json")

  def save(groups: Traversable[SongGroup])(implicit songJsonable: Jsonable[Song]): Unit = groups
    .map(_.songs.jsonify)
    .map(_.toString)
    .mkString("\n") |> jsonFile.write
  def load(implicit songJsonable: Jsonable[Song]): Set[SongGroup] = jsonFile.lines
    .map(_.parseJsonable[Seq[Song]] |> SongGroup.apply)
    .toSet
}

private object SongGroups {
  def fromGroups(groups: Traversable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())((agg, group) =>
      agg ++ group.songs.fproduct(group.const),
    )
}
