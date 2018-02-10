package playlist

import java.util.concurrent.TimeUnit

import common.JsonableSpecs
import models.ArbitraryModels
import org.scalacheck.{Arbitrary, Gen}

import scala.concurrent.duration.Duration

class PlaylistStateTest extends JsonableSpecs {
  import PlaylistState.PlaylistStateJsonable
  import models.ModelJsonable.SongJsonifier

  private implicit val arbPlaylistQueue: Arbitrary[PlaylistState] = Arbitrary(for {
    numberOfSongs <- Gen.choose(1, 100)
    songs <- Gen.listOfN(numberOfSongs, ArbitraryModels.arbSong)
    currentIndex <- Gen.choose(0, numberOfSongs - 1)
    currentDuration <- Gen.choose(0, 1000).map(Duration(_, TimeUnit.SECONDS))
  } yield PlaylistState(songs, currentIndex, currentDuration))

  propJsonTest[PlaylistState]()
}
