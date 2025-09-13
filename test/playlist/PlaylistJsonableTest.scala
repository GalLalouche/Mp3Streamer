package playlist

import java.util.concurrent.TimeUnit

import backend.module.TestModuleConfiguration
import com.google.inject.Injector
import models.{ArbitraryModels, ModelJsonable, Song}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.JsValue
import playlist.Playlist.playlistJsonable

import scala.concurrent.duration.Duration

import common.JsonableSpecs
import common.io.MemoryRoot
import common.json.Jsonable
import common.json.ToJsonableOps.jsonifySingle

class PlaylistJsonableTest extends JsonableSpecs {
  private val injector: Injector = TestModuleConfiguration().injector
  private val mj = injector.instance[ModelJsonable]
  import mj._
  private implicit val root: MemoryRoot = injector.instance[MemoryRoot]
  import playlist.PlaylistJsonableTest.arbPlaylist

  propJsonTest[Playlist]()
}

object PlaylistJsonableTest {
  private implicit def arbPlaylist(implicit root: MemoryRoot): Arbitrary[Playlist] = Arbitrary(for {
    numberOfSongs <- Gen.choose(1, 20)
    songs <- Gen.listOfN(numberOfSongs, ArbitraryModels.arbSong)
    currentIndex <- Gen.choose(0, numberOfSongs - 1)
    currentDuration <- Gen.choose(0, 1000).map(Duration(_, TimeUnit.SECONDS))
  } yield Playlist(songs, currentIndex, currentDuration))

  def arbPlaylistJson(implicit root: MemoryRoot, songJsonable: Jsonable[Song]): Gen[JsValue] =
    arbPlaylist.arbitrary.map(_.jsonify)
}
