package models

import models.ArbitraryModels._
import models.ModelJsonable._
import org.scalatest.tags.Slow

import common.JsonableSpecs

@Slow
class ModelJsonableTest extends JsonableSpecs {
  propJsonTest[Song]()
  propJsonTest[AlbumDir]()
  propJsonTest[Artist]()
}
