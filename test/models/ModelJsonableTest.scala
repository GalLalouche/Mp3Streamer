package models

import models.ArbitraryModels._
import models.ModelJsonable._

import common.JsonableSpecs

class ModelJsonableTest extends JsonableSpecs {
  propJsonTest[Song]()
  propJsonTest[Album]()
  propJsonTest[Artist]()
}
