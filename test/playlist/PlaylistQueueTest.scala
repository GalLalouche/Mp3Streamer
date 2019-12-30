package playlist

import models.ArbitraryModels
import org.scalacheck.Gen

import common.JsonableSpecs

class PlaylistQueueTest extends JsonableSpecs {
  import models.ModelJsonable.SongJsonifier

  private implicit val arbPlaylistQueue: Gen[PlaylistQueue] = for {
    numberOfSongs <- Gen.choose(1, 100)
    songs <- Gen.listOfN(numberOfSongs, ArbitraryModels.arbSong)
  } yield PlaylistQueue(songs)

  propJsonTest[PlaylistQueue]()
}
