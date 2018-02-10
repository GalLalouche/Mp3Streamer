package models

import common.JsonableSpecs
import models.ModelJsonable._

class ModelJsonableTest extends JsonableSpecs {
  import models.ArbitraryModels._

  propJsonTest[Song]()
  propJsonTest[Album]()
  propJsonTest[Artist]()
}
