package common.io

import common.{DirectorySpecs, JsonableSpecs}

class IORefJsonableTest extends JsonableSpecs with DirectorySpecs {
  property("IODirectory is jsonable") {
    val d = IODirectory(tempDir).addSubDir("foo").addSubDir("bar")
    jsonTest(d)
  }
  property("IOFile is jsonable") {
    val f = IODirectory(tempDir).addSubDir("foo").addFile("bar")
    jsonTest(f)
  }
}
