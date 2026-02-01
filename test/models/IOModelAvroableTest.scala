package models

import org.scalatest.OneInstancePerTest
import org.scalatest.tags.Slow

import common.AvroableSpecs
import common.io.avro.ModelAvroable
import common.path.ref.io.IOPathRefFactory

@Slow
class IOModelAvroableTest extends AvroableSpecs with OneInstancePerTest {
  import ModelGenerators._
  private val mj = new ModelAvroable(ModelAvroable.IOSongAvroParser, IOPathRefFactory)
  import mj._
  propAvroTest[Song]()
  propAvroTest[AlbumDir]()
  propAvroTest[ArtistDir]()

  serializationTest[Song]()
  serializationTest[AlbumDir]()
  serializationTest[ArtistDir]()
}
