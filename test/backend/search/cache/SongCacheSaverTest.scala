package backend.search.cache

import backend.module.TestModuleConfiguration
import models.{AlbumDir, ArtistDir, FakeModelFactory, MemorySong, Song}
import musicfinder.{ArtistDirsIndex, FakeMusicFiles, FakeMusicFilesImpl, MusicFiles}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar

import cats.implicits.catsSyntaxOptionId

import common.AvroableSaver
import common.io.avro.ModelAvroable
import common.test.{AuxSpecs, MoreMockitoSugar}
import common.test.memory_ref.MemoryRoot

class SongCacheSaverTest
    extends AnyFreeSpec
    with AuxSpecs
    with MockitoSugar
    with MoreMockitoSugar
    with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private val ma = injector.instance[ModelAvroable]
  private val root = injector.instance[FakeMusicFiles].baseDir
  private val index = mock[ArtistDirsIndex]
  private val saver = injector.instance[AvroableSaver]
  private val factory = new FakeModelFactory(root)
  import ma._

  "saves and updates index" in {
    val s1 = factory.song(artistName = "foo", albumName = "bar", year = 2000, title = "t1")
    val s2 = factory.song(artistName = "foo", albumName = "bar", year = 2000, title = "t2")
    val s3 = factory.song(artistName = "foo", albumName = "bazz", title = "t2")
    val s4 = factory.song(artistName = "moo", albumName = "bar", title = "t1")
    val songs = Vector(s1, s2, s3, s4)
    saveSongs(songs, injector.instance[MusicFiles])

    saver.load[Song] shouldMultiSetEqual songs

    val a1 = AlbumDir(root.addSubDir("foo").addSubDir("bar"), "bar", "foo", s1.year, Vector(s1, s2))
    val a2 = AlbumDir(root.addSubDir("foo").addSubDir("bazz"), "bazz", "foo", s3.year, Vector(s3))
    val a3 = AlbumDir(root.addSubDir("moo").addSubDir("bar"), "bar", "moo", s4.year, Vector(s4))
    saver.load[AlbumDir] shouldMultiSetEqual Vector(a1, a2, a3)

    val artists = Vector(
      ArtistDir(root.addSubDir("foo"), "foo", Set(a1, a2)),
      ArtistDir(root.addSubDir("moo"), "moo", Set(a3)),
    )
    saver.load[ArtistDir] shouldMultiSetEqual artists

    capture[Iterable[ArtistDir]](index)(_.update(_)) shouldMultiSetEqual artists
  }

  "Single album artists (e.g., greatest hits), should have their dirs setup as themselves" in {
    val artistDir = root.addSubDir("genre").addSubDir("foo")
    val s1 = factory.song(
      artistName = "foo",
      albumName = "hits",
      year = 2000,
      title = "t1",
      folder = artistDir.some,
    )
    val a1 =
      AlbumDir(artistDir, title = "hits", artistName = "foo", 2000, Vector(s1))
    saveSongs(Vector(s1), FakeMusicFilesImpl(root, flatGenres = Vector("genre")))
    val artists = Vector(ArtistDir(artistDir, "foo", Set(a1)))
    saver.load[ArtistDir] shouldMultiSetEqual artists
    capture[Iterable[ArtistDir]](index)(_.update(_)) shouldMultiSetEqual artists
  }

  private def saveSongs(songs: Vector[MemorySong], mf: MusicFiles): Unit =
    new SongCacheSaver(saver, index, mf).apply(songs)
}
