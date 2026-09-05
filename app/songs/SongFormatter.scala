package songs

import com.google.inject.Inject
import formatter.ControllerSongJsonifier
import models._
import play.api.libs.json.JsValue
import songs.selector.{FollowingSong, SongSelectorState}

import scala.language.implicitConversions

import common.json.ToJsonableOps._
import common.path.ref.PathRefFactory
import common.rich.RichT._

class SongFormatter @Inject() (
    albumFactory: AlbumDirFactory,
    groups: SongGroups,
    songSelectorState: SongSelectorState,
    followingSong: FollowingSong,
    songTagParser: SongTagParser,
    pathRefFactory: PathRefFactory,
    songJsonifier: ControllerSongJsonifier,
) {
  import songJsonifier.songJsonable

  def randomSong(): JsValue = group(songSelectorState.randomSong()).jsonify
  def randomMp3Song(): JsValue = group(songSelectorState.randomMp3Song()).jsonify
  def randomFlacSong(): JsValue = group(songSelectorState.randomFlacSong()).jsonify

  private def songsInAlbum(path: String): Seq[Song] =
    pathRefFactory.parseDirPath(path) |> albumFactory.fromDir |> AlbumDir.songs.get
  def album(path: String): JsValue = songsInAlbum(path).jsonify
  def discNumber(path: String, requestedDiscNumber: String): JsValue =
    songsInAlbum(path)
      .filter(_.discNumber.contains(requestedDiscNumber))
      .ensuring(_.nonEmpty)
      .jsonify

  def song(path: String): JsValue =
    group(songTagParser(pathRefFactory.parseFilePath(path))).jsonify
  def nextSong(path: String): JsValue =
    followingSong.next(songTagParser(pathRefFactory.parseFilePath(path))).get.jsonify

  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(groups.load)
  private def group(s: Song): Either[Song, SongGroup] = songGroups.get(s).toRight(s)
}
