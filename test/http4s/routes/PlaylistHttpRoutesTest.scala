package http4s.routes

import cats.effect.unsafe.implicits.global
import models.ModelJsonable.SongJsonifier
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.{BeforeAndAfterEach, FreeSpec}
import org.scalatest.tags.Slow
import play.api.libs.json.{JsArray, Json, JsString}
import playlist.{Playlist, PlaylistTest}

import common.io.{DirectoryRef, RootDirectory}
import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}
import common.rich.RichT.richT

@Slow
private class PlaylistHttpRoutesTest extends FreeSpec with Http4sSpecs with BeforeAndAfterEach {
  protected override def afterEach(): Unit = injector.instance[DirectoryRef, RootDirectory].clear()

  "set then get" in {
    val playlist = putArbPlaylist("foobar")
    getPlaylist("foobar") shouldReturn playlist
    getPlaylists shouldContainExactly "foobar"
  }

  "set multiple playlists and get" in {
    val playlist1 = putArbPlaylist("foo")
    val playlist2 = putArbPlaylist("bar")
    getPlaylist("foo") shouldReturn playlist1
    getPlaylist("bar") shouldReturn playlist2
    getPlaylists shouldContainExactly ("bar", "foo")
  }

  "delete" in {
    putArbPlaylist("foo")
    putArbPlaylist("bar")
    getPlaylists shouldContainExactly ("bar", "foo")
    delete[String]("playlist/foo").unsafeRunSync() shouldReturn "true"
    delete[String]("playlist/foo").unsafeRunSync() shouldReturn "false"
    delete[String]("playlist/blabla").unsafeRunSync() shouldReturn "false"
    getPlaylists shouldContainExactly "bar"
  }

  private def putArbPlaylist(name: String): Playlist = {
    val $ = PlaylistTest.arbPlaylist.arbitrary.sample.get
    putJson[String]("playlist/" + name, $.jsonify).unsafeRunSync() shouldReturn name
    $
  }

  private def getPlaylist(name: String): Playlist = {
    val json = get[String]("playlist/" + name) |> Http4sUtils.decode |> Json.parse
    json.parse[Playlist]
  }

  private def getPlaylists: Seq[String] =
    getJson[JsArray]("playlist/").value.map(_.as[JsString].value)
}
