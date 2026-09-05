package songs

import com.google.inject.Inject
import models.{ModelJsonable, Song}

import cats.syntax.functor.toFunctorOps

import common.io.RootDirectory
import common.json.ToJsonableOps._
import common.path.ref.DirectoryRef
import common.rich.RichT._

private class SongGroups @Inject() (
    @RootDirectory rootDirectory: DirectoryRef,
    mj: ModelJsonable,
) {
  private lazy val jsonFile = rootDirectory.addFile("song_groups.json")
  import mj.songJsonifier

  def save(groups: Iterable[SongGroup]): Unit =
    groups.map(_.songs.jsonify).map(_.toString).mkString("\n") |> jsonFile.write
  def load: Set[SongGroup] = jsonFile.lines.map(_.parseJsonable[Seq[Song]] |> SongGroup.apply).toSet
}

private object SongGroups {
  def fromGroups(groups: Iterable[SongGroup]): Map[Song, SongGroup] =
    groups.foldLeft(Map[Song, SongGroup]())((agg, group) =>
      agg ++ group.songs.fproduct(group.const),
    )
}
