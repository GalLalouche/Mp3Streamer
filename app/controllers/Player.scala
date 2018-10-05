package controllers

import common.Debug
import common.io.IODirectory
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.ControllerUtils.songJsonable
import decoders.Mp3Encoder
import javax.inject.Inject
import models._
import play.api.libs.json.JsValue
import play.api.mvc._
import songs.{SongGroup, SongGroups, SongSelectorState}

/** Handles fetch requests of JSON information. */
class Player @Inject()(
    albumFactory: AlbumFactory,
    groups: SongGroups,
    songSelectorState: SongSelectorState,
    encoder: Mp3Encoder,
    urlPathUtils: UrlPathUtils,
) extends InjectedController with ToJsonableOps with Debug {
  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(groups.load)

  private def group(s: Song): Either[Song, SongGroup] = songGroups get s toRight s

  // Doesn't extend JsonWritable to avoid recursive implicit lookup.
  private trait Encodable[A] {
    def encode(e: A): Unit
    def jsonify(e: A): JsValue
  }
  private object Encodable {
    private def jsonableEncodable[A: JsonWriteable](f: A => Unit): Encodable[A] =
      new Encodable[A] {
        override def encode(e: A): Unit = f(e)
        override def jsonify(a: A): JsValue = a.jsonify
      }
    implicit val songEncodable: Encodable[Song] = jsonableEncodable(encoder ! _.file)
    implicit val songsEncodable: Encodable[Seq[Song]] =
      jsonableEncodable(_ foreach songEncodable.encode)
    implicit val eitherEncodable: Encodable[Either[Song, SongGroup]] =
      jsonableEncodable(_.fold(songEncodable.encode, songsEncodable encode _.songs))
  }
  private def encodeIfChrome[A](encodable: A)(request: Request[_])(implicit ev: Encodable[A]): Result = {
    if (ControllerUtils.shouldEncodeMp3(request))
      ev.encode(encodable)
    Ok(ev.jsonify(encodable))
  }

  def randomSong = Action {
    encodeIfChrome(group(songSelectorState.randomSong)) _
  }

  // For debugging
  def randomMp3Song = Action {
    encodeIfChrome(group(songSelectorState.randomMp3Song)) _
  }
  def randomFlacSong = Action {
    encodeIfChrome(group(songSelectorState.randomFlacSong)) _
  }

  private def songsInAlbum(path: String): Seq[Song] =
    urlPathUtils.parseFile(path) |> IODirectory.apply |> albumFactory.fromDir |> Album.songs.get
  def album(path: String) = Action {
    encodeIfChrome(songsInAlbum(path)) _
  }
  def discNumber(path: String, requestedDiscNumber: String) = Action {
    val songsWithDiscNumber =
      songsInAlbum(path).filter(_.discNumber.exists(requestedDiscNumber ==)).ensuring(_.nonEmpty)
    encodeIfChrome(songsWithDiscNumber) _
  }

  def song(path: String) = Action {
    encodeIfChrome(group(urlPathUtils parseSong path)) _
  }

  def nextSong(path: String) = Action {
    encodeIfChrome(songSelectorState.followingSong(urlPathUtils.parseSong(path)).get) _
  }
}
