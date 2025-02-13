package playlist

import java.util.concurrent.TimeUnit

import models.ArbitraryModels
import org.scalacheck.{Arbitrary, Gen}

import scala.concurrent.duration.Duration

import common.JsonableSpecs

class PlaylistTest extends JsonableSpecs {
  import PlaylistTest.arbPlaylist
  import models.ModelJsonable.SongJsonifier

  propJsonTest[Playlist]()
}

object PlaylistTest {
  implicit val arbPlaylist: Arbitrary[Playlist] = Arbitrary(for {
    numberOfSongs <- Gen.choose(1, 20)
    songs <- Gen.listOfN(numberOfSongs, ArbitraryModels.arbSong)
    currentIndex <- Gen.choose(0, numberOfSongs - 1)
    currentDuration <- Gen.choose(0, 1000).map(Duration(_, TimeUnit.SECONDS))
  } yield Playlist(songs, currentIndex, currentDuration))
}
