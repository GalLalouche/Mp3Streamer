package playlist

import backend.module.TestModuleConfiguration
import controllers.{ControllerSpec, UrlDecodeUtils}
import models.ModelJsonable.SongJsonifier
import org.scalatest.{BeforeAndAfterEach, FreeSpec}
import org.scalatest.tags.Slow
import play.api.inject.{BindingKey, QualifierClass}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json, JsString}

import common.io.{DirectoryRef, RootDirectory}
import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}
import common.rich.RichFuture.richFuture
import common.rich.RichT.richT

@Slow
class PlaylistControllerTest extends FreeSpec with ControllerSpec with BeforeAndAfterEach {
  override def fakeApplication() =
    GuiceApplicationBuilder().overrides(TestModuleConfiguration().module).build

  private lazy val inj = app.injector
  private val decoder = inj.instanceOf[UrlDecodeUtils]

  protected override def afterEach(): Unit =
    inj
      .instanceOf(
        new BindingKey(classOf[DirectoryRef], Some(QualifierClass(classOf[RootDirectory]))),
      )
      .clear()

  "set then get" in {
    val playlist = arbPlaylist
    put("playlist/foobar", playlist.jsonify).getString shouldReturn "foobar"
    getPlaylist("foobar") shouldReturn playlist
  }

  "set multiple playlists and get" in {
    val playlist1 = arbPlaylist
    val playlist2 = arbPlaylist
    put("playlist/foo", playlist1.jsonify).getString shouldReturn "foo"
    put("playlist/bar", playlist2.jsonify).getString shouldReturn "bar"
    getPlaylist("foo") shouldReturn playlist1
    getPlaylist("bar") shouldReturn playlist2
    getPlaylists shouldContainExactly ("bar", "foo")
  }

  "delete" in {
    val playlist1 = arbPlaylist
    val playlist2 = arbPlaylist
    put("playlist/foo", playlist1.jsonify).getString shouldReturn "foo"
    put("playlist/bar", playlist2.jsonify).getString shouldReturn "bar"
    getPlaylists shouldContainExactly ("bar", "foo")
    delete("playlist/foo").getString shouldReturn "true"
    delete("playlist/foo").getString shouldReturn "false"
    delete("playlist/blabla").getString shouldReturn "false"
    getPlaylists shouldContainExactly "bar"
  }

  private def arbPlaylist = PlaylistTest.arbPlaylist.arbitrary.sample.get

  private def getPlaylist(name: String) = {
    val result = get("playlist/" + name).get
    if (result.status != 200)
      fail(s"Get failed!:\n" + result.body)
    val json = result.body |> decoder.apply |> Json.parse
    json.parse[Playlist]
  }

  private def getPlaylists: Seq[String] =
    get("playlist/").get.json.as[JsArray].value.map(_.as[JsString].value)
}
