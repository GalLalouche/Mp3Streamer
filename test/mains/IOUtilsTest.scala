package mains

import common.io.MemoryRoot
import common.test.AuxSpecs
import org.scalatest.FreeSpec

class IOUtilsTest extends FreeSpec with AuxSpecs {
  "decode" in {
    val dir = new MemoryRoot().addSubDir("foo").addSubDir("bar")
    val target = dir.addFile("mאo")
    dir.addFile("oink")
    dir.addFile("maa")
    IOUtils.decode(dir, "m?o") shouldReturn target
  }
}
