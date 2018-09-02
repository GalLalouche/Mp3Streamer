package controllers

import common.Debug
import common.io.IODirectory
import common.json.{JsonWriteable, ToJsonableOps}
import common.rich.RichT._
import controllers.ControllerUtils.songJsonable
import decoders.DbPowerampCodec
import models._
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.JsValue
import play.api.mvc._
import songs.{SongGroup, SongGroups, SongSelectorState}

import scala.annotation.tailrec

/** Handles fetch requests of JSON information, and listens to directory changes. */
object Player extends LegacyController with ToJsonableOps with Debug {
  private val albumFactory = injector.instance[AlbumFactory]
  private val songGroups: Map[Song, SongGroup] = SongGroups.fromGroups(
    injector.instance[SongGroups].load)
  private val encoder = DbPowerampCodec
  private val songSelectorState: SongSelectorState = injector.instance[SongSelectorState]

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
    implicit val songEncodable: Encodable[Song] =
      jsonableEncodable(encoder ! _.asInstanceOf[IOSong].file.file)
    implicit val songsEncodable: Encodable[Seq[Song]] =
      jsonableEncodable(_ foreach songEncodable.encode)
    implicit val eitherEncodable: Encodable[Either[Song, SongGroup]] =
      jsonableEncodable(_.fold(songEncodable.encode, songsEncodable encode _.songs))
  }
  private def encodeIfChrome[A](encodable: A)(request: Request[_])(implicit ev: Encodable[A]): Result = {
    if (ControllerUtils.encodeMp3(request))
      ev.encode(encodable)
    Ok(ev.jsonify(encodable))
  }

  def randomSong = Action {
    encodeIfChrome(group(songSelectorState.randomSong)) _
  }

  // For debugging
  // TODO move this to songSelector
  // TODO handle code duplication with below
  def randomMp3Song = Action {
    @tailrec
    def aux: Song = {
      val $ = songSelectorState.randomSong
      if ($.file.extension == "mp3") $ else aux
    }
    encodeIfChrome(group(aux)) _
  }

  def randomFlacSong = Action {
    @tailrec
    def aux: Song = {
      val $ = songSelectorState.randomSong
      if ($.file.extension == "flac") $ else aux
    }
    encodeIfChrome(group(aux)) _
  }

  private def songsInAlbum(path: String): Seq[Song] =
    ControllerUtils.parseFile(path) |> IODirectory.apply |> albumFactory.fromDir |> Album.songs.get
  def album(path: String) = Action {
    encodeIfChrome(songsInAlbum(path)) _
  }
  def discNumber(path: String, requestedDiscNumber: String) = Action {
    val songsWithDiscNumber =
      songsInAlbum(path).filter(_.discNumber.exists(requestedDiscNumber ==)).ensuring(_.nonEmpty)
    encodeIfChrome(songsWithDiscNumber) _
  }

  def song(path: String) = Action {
    encodeIfChrome(group(ControllerUtils parseSong path)) _
  }

  def nextSong(path: String) = Action {
    encodeIfChrome(songSelectorState.followingSong(ControllerUtils.parseSong(path)).get) _
  }
}
