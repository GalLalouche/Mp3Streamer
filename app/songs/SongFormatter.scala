package songs

import java.io.File
import javax.inject.Inject

import controllers.ControllerSongJsonifier
import models._
import play.api.libs.json.JsValue
import song_encoder.Mp3Encoder
import songs.SongFormatter.ShouldEncodeMp3Reader
import songs.selector.{FollowingSong, SongSelectorState}

import scala.language.implicitConversions

import scalaz.Reader

import common.io.IODirectory
import common.json.JsonWriteable
import common.json.ToJsonableOps._
import common.rich.RichT._

private class SongFormatter @Inject() (
    albumFactory: AlbumDirFactory,
    groups: SongGroups,
    songSelectorState: SongSelectorState,
    followingSong: FollowingSong,
    encoder: Mp3Encoder,
    songJsonifier: ControllerSongJsonifier,
) {
  import songJsonifier.songJsonable

  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(groups.load)
  private def group(s: Song): Either[Song, SongGroup] = songGroups.get(s).toRight(s)

  // Doesn't extend JsonWritable to avoid recursive implicit lookup.
  private trait Encodable[E] {
    def encode(e: E): Unit
    def jsonify(e: E): JsValue
    def reader(e: E): ShouldEncodeMp3Reader = Reader { b =>
      if (b)
        encode(e)
      jsonify(e)
    }
  }
  private object Encodable {
    private def jsonableEncodable[A: JsonWriteable](f: A => Unit): Encodable[A] =
      new Encodable[A] {
        override def encode(e: A): Unit = f(e)
        override def jsonify(a: A): JsValue = a.jsonify
      }
    implicit val songEncodable: Encodable[Song] = jsonableEncodable(encoder ! _.file)
    implicit val songsEncodable: Encodable[Seq[Song]] =
      jsonableEncodable(_.foreach(songEncodable.encode))
    implicit val eitherEncodable: Encodable[Either[Song, SongGroup]] =
      jsonableEncodable(_.fold(songEncodable.encode, songsEncodable encode _.songs))
  }
  private implicit def encodableReader[E: Encodable]($ : E): ShouldEncodeMp3Reader =
    implicitly[Encodable[E]].reader($)

  def randomSong(): ShouldEncodeMp3Reader = group(songSelectorState.randomSong())
  def randomMp3Song(): ShouldEncodeMp3Reader = group(songSelectorState.randomMp3Song())
  def randomFlacSong(): ShouldEncodeMp3Reader = group(songSelectorState.randomFlacSong())

  private def songsInAlbum(path: String): Seq[Song] =
    new File(path) |> IODirectory.apply |> albumFactory.fromDir |> AlbumDir.songs.get
  def album(path: String): ShouldEncodeMp3Reader = songsInAlbum(path)
  def discNumber(path: String, requestedDiscNumber: String): ShouldEncodeMp3Reader =
    songsInAlbum(path).filter(_.discNumber.contains(requestedDiscNumber)).ensuring(_.nonEmpty)

  def song(path: String): ShouldEncodeMp3Reader = group(IOSong.read(new File(path)))
  def nextSong(path: String): ShouldEncodeMp3Reader =
    followingSong.next(IOSong.read(new File(path))).get
}

object SongFormatter {
  type ShouldEncodeMp3Reader = Reader[Boolean, JsValue]
}
