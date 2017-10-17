package common

import common.Jsonable.ToJsonableOps
import org.scalatest.FreeSpec

trait JsonableSpecs extends FreeSpec with AuxSpecs with ToJsonableOps {
  def jsonTest[T: Jsonable](t: T): Unit = {
    t.jsonify.parse[T] shouldReturn t
  }
}
