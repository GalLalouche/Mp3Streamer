package server

import formatter.UrlDecoder
import models.ModelJsonable.SongJsonifier
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.OneInstancePerTest
import org.scalatest.tags.Slow
import play.api.libs.json.{JsArray, Json, JsString}
import playlist.{Playlist, PlaylistTest}
import sttp.client3.UriContext

import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.{ToBindOps, ToFunctorOps}

import common.json.ToJsonableOps.{jsonifySingle, parseJsValue}

@Slow
private class PlaylistTest extends Http4sEndToEndSpecs with OneInstancePerTest {
  "set then get" in {
    putArbPlaylist("foobar") >>= (playlist =>
      checkAll(
        getPlaylist("foobar") shouldEventuallyReturn playlist,
        getPlaylists shouldEventuallyReturn Vector("foobar"),
      ),
    )
  }

  "override" in {
    for {
      _ <- putArbPlaylist("foobar")
      pl1 <- putArbPlaylist("foobar")
      result <- checkAll(
        getPlaylist("foobar") shouldEventuallyReturn pl1,
        getPlaylists shouldEventuallyReturn Vector("foobar"),
      )
    } yield result
  }

  "set multiple playlists and get" in {
    for {
      playlist1 <- putArbPlaylist("foo")
      playlist2 <- putArbPlaylist("bar")
      result <- checkAll(
        getPlaylist("foo") shouldEventuallyReturn playlist1,
        getPlaylist("bar") shouldEventuallyReturn playlist2,
        getPlaylists shouldEventuallyReturn Vector("bar", "foo"),
      )
    } yield result
  }

  "delete" in {
    putArbPlaylist("foo") >> putArbPlaylist("bar") >> checkAll(
      getPlaylists shouldEventuallyReturn Vector("bar", "foo"),
      deleteString(uri"playlist/foo") shouldEventuallyReturn "true",
      deleteString(uri"playlist/foo") shouldEventuallyReturn "false",
      deleteString(uri"playlist/blabla") shouldEventuallyReturn "false",
      getPlaylists shouldEventuallyReturn Vector("bar"),
    )
  }

  private def putArbPlaylist(name: String): Future[Playlist] = {
    val $ = PlaylistTest.arbPlaylist.arbitrary.sample.get
    (putJson(uri"playlist/$name", $.jsonify) shouldEventuallyReturn name) >| $
  }

  private def getPlaylist(name: String): Future[Playlist] =
    getString(uri"playlist/$name")
      .map(injector.instance[UrlDecoder].apply)
      .map(Json.parse(_).parse[Playlist])

  private def getPlaylists: Future[Seq[String]] =
    getJson(uri"playlist/").map(_.as[JsArray].value.map(_.as[JsString].value))
}
