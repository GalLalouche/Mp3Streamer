package backend.recon

import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec

import common.test.AuxSpecs
import common.test.memory_ref.MemoryRoot

class ReconcilableFactoryTest extends AnyFreeSpec with AuxSpecs {
  "Parsing song info from file path" - {
    "dash syntax " in {
      ReconcilableFactory.capture("01 - foo bar.mp3").value shouldReturn (1, "foo bar")
    }
    "dot syntax" in {
      ReconcilableFactory.capture("01. foo bar.flac").value shouldReturn (1, "foo bar")
    }
  }
  "hasYearPrefix" - {
    val root = new MemoryRoot
    def dir(name: String) = root.addSubDir(name)
    def test(name: String, expected: Boolean): Unit = s"<$name> should be <$expected>" in {
      ReconcilableFactory.hasYearPrefix(dir(name)) shouldReturn expected
    }
    test("2020 Foo Bar", expected = true)
    test("1969A Foo Bar", expected = true)
    test("2005", expected = false)
    test("2020B", expected = false)
    test("2020 ", expected = false)
  }
}
