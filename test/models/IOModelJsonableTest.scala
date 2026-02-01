package models

import org.scalatest.OneInstancePerTest
import org.scalatest.tags.Slow

import common.JsonableSpecs
import common.path.ref.io.IOPathRefFactory

@Slow
class IOModelJsonableTest extends JsonableSpecs with OneInstancePerTest {
  import ModelGenerators._
  private val mj = new ModelJsonable(ModelJsonable.IOSongJsonParser, IOPathRefFactory)
  import mj._
  propJsonTest[Song]()
  propJsonTest2[AlbumDir]()
  propJsonTest[ArtistDir]()
}
