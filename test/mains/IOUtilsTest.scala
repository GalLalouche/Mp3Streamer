package mains

import org.scalatest.FreeSpec

import common.io.MemoryRoot
import common.test.AuxSpecs

class IOUtilsTest extends FreeSpec with AuxSpecs {
  "decode" in {
    val dir = new MemoryRoot().addSubDir("foo").addSubDir("bar")
    val target = dir.addFile("mאo")
    dir.addFile("oink")
    dir.addFile("maa")
    IOUtils.decode(dir, "m?o") shouldReturn target
  }
}
