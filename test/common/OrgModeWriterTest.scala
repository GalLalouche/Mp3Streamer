package common

import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs

class OrgModeWriterTest extends AnyFreeSpec with AuxSpecs {
  "Basic example" in {
    OrgModeWriter()
      .append("foo")
      .append("bar")
      .indent(
        _.append("bazz")
          .append("oink")
          .indent(_.append("moo")),
      )
      .append("end")
      .lines should contain theSameElementsInOrderAs Vector(
      "* foo",
      "* bar",
      "** bazz",
      "** oink",
      "*** moo",
      "* end",
    )
  }
}
