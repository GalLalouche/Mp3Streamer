package mains

import common.test.AuxSpecs
import common.test.memory_ref.MemoryRoot
import org.scalatest.freespec.AnyFreeSpec

class IOUtilsTest extends AnyFreeSpec with AuxSpecs {
  "decode" in {
    val dir = new MemoryRoot().addSubDir("foo").addSubDir("bar")
    val target = dir.addFile("mאo")
    dir.addFile("oink")
    dir.addFile("maa")
    IOUtils.decode(dir, "m?o") shouldReturn target
  }
}
