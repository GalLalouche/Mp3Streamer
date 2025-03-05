package songs

import java.io.File
import com.google.inject.Inject

import formatter.ControllerSongJsonifier
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

class SongFormatter @Inject() (
    albumFactory: AlbumDirFactory,
    groups: SongGroups,
    songSelectorState: SongSelectorState,
    followingSong: FollowingSong,
    mp3Encoder: Mp3Encoder,
    songJsonifier: ControllerSongJsonifier,
) {
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

  import songJsonifier.songJsonable

  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(groups.load)
  private def group(s: Song): Either[Song, SongGroup] = songGroups.get(s).toRight(s)

  // Doesn't extend JsonWritable to avoid recursive implicit lookup.
  private trait Mp3Encodable[E] {
    def encode(e: E): Unit
    def jsonify(e: E): JsValue
    def reader(e: E): ShouldEncodeMp3Reader = Reader { b =>
      if (b)
        // Encodes MP3 as a side effect. This assumes that the few seconds it takes to encode the
        // MP3 will be swallowed by the fact the that most songs are asked for a few seconds at
        // least before being actually played.
        encode(e)
      jsonify(e)
    }
  }
  private object Mp3Encodable {
    private def jsonableEncodable[A: JsonWriteable](f: A => Unit): Mp3Encodable[A] =
      new Mp3Encodable[A] {
        override def encode(e: A): Unit = f(e)
        override def jsonify(a: A): JsValue = a.jsonify
      }
    implicit val songEncodable: Mp3Encodable[Song] = jsonableEncodable(mp3Encoder ! _.file)
    implicit val songsEncodable: Mp3Encodable[Seq[Song]] =
      jsonableEncodable(_.foreach(songEncodable.encode))
    implicit val eitherEncodable: Mp3Encodable[Either[Song, SongGroup]] =
      jsonableEncodable(_.fold(songEncodable.encode, songsEncodable encode _.songs))
  }
  private implicit def encodableReader[E: Mp3Encodable]($ : E): ShouldEncodeMp3Reader =
    implicitly[Mp3Encodable[E]].reader($)
}

object SongFormatter {
  type ShouldEncodeMp3Reader = Reader[Boolean, JsValue]
}
