package songs

import backend.configs.{FakeMusicFinder, TestConfiguration}
import common.AuxSpecs
import models.MemorySong
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}
import search.FakeModelFactory

class SongSelectorTest extends FreeSpec with OneInstancePerTest with AuxSpecs with GeneratorDrivenPropertyChecks
    with ShouldMatchers {
  implicit override val generatorDrivenConfig = PropertyCheckConfig(minSuccessful = 10, workers = 5)
  private val factory = new FakeModelFactory()
  private implicit def genToArb[T: Gen]: Arbitrary[T] = Arbitrary(implicitly[Gen[T]])
  private implicit lazy val arbSong: Gen[MemorySong] = for {
    title <- arbitrary[String]
    artistName <- arbitrary[String]
    albumName <- arbitrary[String]
    track <- arbitrary[Int].map(_ % 100)
    year <- arbitrary[Int].map(_ % 3000)
    bitRate <- arbitrary[Int].map(_ % 10000).map(_ / 32.0).map(_.toString)
    duration <- arbitrary[Int].map(_ % 1000)
    size <- arbitrary[Int]
    discNumber <- arbitrary[Option[String]]
    trackGain <- arbitrary[Option[Int]].map(_.map(_ % 10000).map(_ / 32.0))
  } yield factory.song(
    title = title, artistName = artistName, albumName = albumName, track = track, year = year,
    bitRate = bitRate, duration = duration, size = size, discNumber = discNumber, trackGain = trackGain)

  "returns a random song" in {
    forAll {ss: List[MemorySong] =>
      whenever(ss.nonEmpty) {
        implicit val c = new TestConfiguration
        val mf: FakeMusicFinder = c.mf
        val songs = ss.map(mf.copySong)
        val $ = SongSelector.create
        songs should contain($.randomSong)
      }
    }
  }
  "next song" - {
    "exists" in {
      forAll {(s1: MemorySong, s2: MemorySong) => {
        implicit val c = new TestConfiguration
        val mf: FakeMusicFinder = c.mf
        val song1 = mf.copySong(s1.copy(albumName = "album", artistName = "artist", track = 1))
        val song2 = mf.copySong(s2.copy(albumName = "album", artistName = "artist", track = 2))
        val $ = SongSelector.create
        $.followingSong(song1).get shouldReturn song2
      }
      }
    }
    "doesn't exist" in {
      forAll {(s1: MemorySong, s2: MemorySong) => {
        implicit val c = new TestConfiguration
        val mf: FakeMusicFinder = c.mf
        val song1 = mf.copySong(s1.copy(albumName = "album1", artistName = "artist", track = 1))
        mf.copySong(s2.copy(albumName = "album2", artistName = "artist", track = 2))
        val $ = SongSelector.create
        $.followingSong(song1) shouldBe 'empty
      }
      }
    }
  }
}
