package playlist

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

import common.JsonableSpecs
import models.ArbitraryModels
import org.scalacheck.{Arbitrary, Gen}

class PlaylistStateTest extends JsonableSpecs {
  import models.ModelJsonable.SongJsonifier
  import PlaylistState.PlaylistStateJsonable

  private implicit val arbPlaylistQueue: Arbitrary[PlaylistState] = Arbitrary(for {
    numberOfSongs <- Gen.choose(1, 100)
    songs <- Gen.listOfN(numberOfSongs, ArbitraryModels.arbSong)
    currentIndex <- Gen.choose(0, numberOfSongs - 1)
    currentDuration <- Gen.choose(0, 1000).map(Duration(_, TimeUnit.SECONDS))
  } yield PlaylistState(songs, currentIndex, currentDuration))

  propJsonTest[PlaylistState]()
}
