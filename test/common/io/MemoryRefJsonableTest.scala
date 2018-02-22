package common.io

import backend.configs.TestConfiguration
import common.JsonableSpecs
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

class MemoryRefJsonableTest extends JsonableSpecs {
  private implicit val c: TestConfiguration = TestConfiguration()
  private val root: MemoryRoot = c.rootDirectory
  private implicit val pathName: Arbitrary[String] = Arbitrary(Gen.alphaNumStr.filter(_.nonEmpty))
  private def genMemoryDir(dir: MemoryDir): Gen[MemoryDir] = for {
    nextName <- arbitrary[String]
    isFinal <- arbitrary[Boolean]
    result <- if (isFinal) genMemoryDir(dir addSubDir nextName) else Gen.const(dir)
  } yield result
  private implicit val arbMemoryDir: Arbitrary[MemoryDir] = Arbitrary(genMemoryDir(root))
  propJsonTest[MemoryDir]()

  private implicit val arbMemoryFile: Arbitrary[MemoryFile] = Arbitrary(for {
    dir <- arbitrary[MemoryDir]
    fileName <- arbitrary[String]
  } yield dir addFile fileName)
  propJsonTest[MemoryFile]()

  property("Does not create files") {
    val f = root.addFile("foobar")
    val json = f.jsonify
    f.delete()
    a[NoSuchElementException] should be thrownBy json.parse[MemoryFile]
  }

  property("Does not create dirs") {
    val dir = root.addSubDir("foo")
    val json = dir.addFile("foobar").jsonify
    dir.deleteAll()
    a[NoSuchElementException] should be thrownBy json.parse[MemoryFile]
  }
}
