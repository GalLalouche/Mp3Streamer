package models

import org.scalatest.tags.Slow

import common.JsonableSpecs
import models.ArbitraryModels._
import models.ModelJsonable._

@Slow
class ModelJsonableTest extends JsonableSpecs {
  propJsonTest[Song]()
  propJsonTest[Album]()
  propJsonTest[Artist]()
}
