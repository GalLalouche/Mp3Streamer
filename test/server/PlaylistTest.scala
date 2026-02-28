package server

import com.google.inject.Module
import formatter.UrlDecoder
import models.ModelJsonable
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import play.api.libs.json.{JsArray, Json, JsString, JsValue}
import playlist.PlaylistJsonableTest
import sttp.client3.UriContext

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.io.RootDirectory
import common.json.RichJson.ImmutableJsonArray
import common.json.ToJsonableOps.jsonifySingle
import common.path.ref.DirectoryRef
import common.test.BeforeAndAfterEachAsync
import common.test.memory_ref.MemoryRoot

private class PlaylistTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {
  override def afterEach(): Future[Unit] = Future.successful(injector.instance[DirectoryRef, RootDirectory].clear())
  "set then get" in {
    putArbPlaylist("foobar") >>= (playlist =>
      checkAll(
        getPlaylist("foobar").map(_ shouldContain playlist),
        getPlaylists shouldEventuallyReturn Vector("foobar"),
      ),
    )
  }

  "override" in {
    for {
      _ <- putArbPlaylist("foobar")
      pl1 <- putArbPlaylist("foobar")
      result <- checkAll(
        getPlaylist("foobar").map(_ shouldContain pl1),
        getPlaylists shouldEventuallyReturn Vector("foobar"),
      )
    } yield result
  }

  "set multiple playlists and get" in {
    for {
      playlist1 <- putArbPlaylist("foo")
      playlist2 <- putArbPlaylist("bar")
      result <- checkAll(
        getPlaylist("foo").map(_ shouldContain playlist1),
        getPlaylist("bar").map(_ shouldContain playlist2),
        getPlaylists shouldEventuallyReturn Vector("bar", "foo"),
      )
    } yield result
  }

  "delete existing playlist removes it" in {
    for {
      _ <- putArbPlaylist("foo")
      _ <- putArbPlaylist("bar")
      result <- deleteString(uri"playlist/foo")
      remaining <- getPlaylists
    } yield {
      result shouldReturn "true"
      remaining shouldReturn Vector("bar")
    }
  }

  "delete already-deleted playlist returns false" in {
    putArbPlaylist("foo") *>>
      deleteString(uri"playlist/foo") *>>
      deleteString(uri"playlist/foo") shouldEventuallyReturn "false"
  }

  "delete non-existent playlist returns false" in {
    deleteString(uri"playlist/blabla") shouldEventuallyReturn "false"
  }

  // TODO check both IO add memory?
  private val mj = injector.instance[ModelJsonable]
  import mj.songJsonifier
  private implicit val root: MemoryRoot = injector.instance[MemoryRoot]
  private def putArbPlaylist(name: String): Future[JsValue] = {
    val $ = PlaylistJsonableTest.arbPlaylistJson.sample.get
    (putString(uri"playlist/$name", $.jsonify) shouldEventuallyReturn name) >| $
  }

  private def getPlaylist(name: String): Future[JsValue] =
    getString(uri"playlist/$name").map(injector.instance[UrlDecoder].apply).map(Json.parse)

  private def getPlaylists: Future[Vector[String]] =
    getJson(uri"playlist/").map(_.as[JsArray].map(_.as[JsString].value).toVector)
}
