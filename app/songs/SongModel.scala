package songs

import com.google.inject.Inject
import formatter.ControllerSongJsonifier
import models._
import songs.SongModel.SongOrGroup
import songs.selector.{FollowingSong, SongSelectorState}

import scala.language.implicitConversions

import common.path.ref.PathRefFactory
import common.rich.RichT._

private class SongModel @Inject() (
    albumFactory: AlbumDirFactory,
    groups: SongGroups,
    songSelectorState: SongSelectorState,
    followingSong: FollowingSong,
    songTagParser: SongTagParser,
    pathRefFactory: PathRefFactory,
    songJsonifier: ControllerSongJsonifier,
) {
  def randomSong(): SongOrGroup = group(songSelectorState.randomSong())
  def randomMp3Song(): SongOrGroup = group(songSelectorState.randomMp3Song())
  def randomFlacSong(): SongOrGroup = group(songSelectorState.randomFlacSong())

  private def songsInAlbum(path: String): Seq[Song] =
    pathRefFactory.parseDirPath(path) |> albumFactory.fromDir |> AlbumDir.songs.get
  def album(path: String): Seq[Song] = songsInAlbum(path)
  def discNumber(path: String, requestedDiscNumber: String): Seq[Song] =
    songsInAlbum(path)
      .filter(_.discNumber.contains(requestedDiscNumber))
      .ensuring(_.nonEmpty)

  def song(path: String): SongOrGroup =
    group(songTagParser(pathRefFactory.parseFilePath(path)))
  def nextSong(path: String): Song =
    followingSong.next(songTagParser(pathRefFactory.parseFilePath(path))).get

  import songJsonifier.songJsonable
  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(groups.load)
  private def group(s: Song): SongOrGroup = songGroups.get(s).toRight(s)
}

private object SongModel {
  type SongOrGroup = Either[Song, SongGroup]
}
