package backend.search.cache

import backend.module.TestModuleConfiguration
import models.{AlbumDir, ArtistDir, FakeModelFactory, ModelJsonable, Song}
import musicfinder.ArtistDirsIndex
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar

import common.io.{JsonableSaver, MemoryRoot}
import common.test.AuxSpecs

class SongCacheSaverTest extends AnyFreeSpec with AuxSpecs with MockitoSugar {
  "saves and updates index" in {
    val injector = TestModuleConfiguration().injector
    val mj = injector.instance[ModelJsonable]
    import mj._
    val factory = new FakeModelFactory(injector.instance[MemoryRoot])
    val s1 = factory.song(artistName = "foo", albumName = "bar", year = 2000, title = "t1")
    val s2 = factory.song(artistName = "foo", albumName = "bar", year = 2000, title = "t2")
    val s3 = factory.song(artistName = "foo", albumName = "bazz", title = "t2")
    val s4 = factory.song(artistName = "moo", albumName = "bar", title = "t1")
    val songs = Vector(s1, s2, s3, s4)
    val index = mock[ArtistDirsIndex]
    val saver = injector.instance[JsonableSaver]
    new SongCacheSaver(saver, index).apply(songs)

    saver.loadArray[Song] shouldMultiSetEqual songs

    val root = injector.instance[MemoryRoot]
    val a1 = AlbumDir(root.addSubDir("foo").addSubDir("bar"), "bar", "foo", s1.year, Vector(s1, s2))
    val a2 = AlbumDir(root.addSubDir("foo").addSubDir("bazz"), "bazz", "foo", s3.year, Vector(s3))
    val a3 = AlbumDir(root.addSubDir("moo").addSubDir("bar"), "bar", "moo", s4.year, Vector(s4))
    saver.loadArray[AlbumDir] shouldMultiSetEqual Vector(a1, a2, a3)

    val artists = Vector(
      ArtistDir(root.addSubDir("foo"), "foo", Set(a1, a2)),
      ArtistDir(root.addSubDir("moo"), "moo", Set(a3)),
    )
    saver.loadArray[ArtistDir] shouldMultiSetEqual artists

    // TODO MoreMockitoSugar?
    val argument = ArgumentCaptor.forClass(classOf[Iterable[ArtistDir]]);
    Mockito.verify(index, Mockito.times(1)).update(argument.capture())
    argument.getValue shouldMultiSetEqual artists
  }
}
