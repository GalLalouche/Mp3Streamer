package backend.logging

import org.scalatest.{Assertion, BeforeAndAfterEach}
import org.scalatest.freespec.AnyFreeSpec
import scribe.{Level, Logger}

import scala.collection.mutable.ArrayBuffer

import common.test.AuxSpecs

class ScribeUtilsTest extends AnyFreeSpec with AuxSpecs with BeforeAndAfterEach {
  import backend.logging.ScribeUtilsTest._
  private val logs = new ArrayBuffer[String]()
  protected override def beforeEach(): Unit = {
    logs.clear()
    Logger.reset()
    Logger.root
      .clearHandlers()
      .clearModifiers()
      .withHandler(writer = (record, _, _) => logs ++= record.messages.map(_.value.toString))
      .replace()
  }

  // This test is ignored by default because the scribe logger is a global state and it can cause
  // flakiness if other tests are also using it. If you wish to run it, change "shouldRun" to true.
  // TODO generalize
  private val shouldRun = false
  private val runner = if (shouldRun) "Unignored wrapped" - _ else "Ignored by default" ignore _
  runner {
    "Order shouldn't matter (more permissive)" - {
      def test(setLevels: Seq[() => Any]): Assertion = {
        setLevels.foreach(_())
        logInfo()
        logDebug()
        logs.toVector shouldReturn Vector("Foo info", "Bar debug")
      }

      def testBoth(setLevels: Seq[() => Any]): Unit = {
        "Info then Debug" in test(setLevels)
        "Debug then Info" in test(setLevels.reverse)
      }

      "Built-in method" - { // This is here for documentation.
        testBoth(
          Vector(
            () => Logger(InnerPackage).withMinimumLevel(Level.Info).replace(),
            () => Logger(BarPath).withMinimumLevel(Level.Debug).replace(),
          ),
        )
      }
      "ScribeUtils" - {
        testBoth(
          Vector(
            () => ScribeUtils.setLevel(InnerPackage, Level.Info),
            () => ScribeUtils.setLevel(BarPath, Level.Debug),
          ),
        )
      }
    }
    "Order doesn't matter (less permissive)" - {
      def test(setLevels: Seq[() => Any]): Assertion = {
        setLevels.foreach(_())
        logInfo()
        logDebug()
        logs.toVector shouldReturn Vector("Foo info")
      }

      def testBoth(setLevels: Seq[() => Any]): Unit = {
        "Info then Debug" in test(setLevels)
        "Debug then Info" in test(setLevels.reverse)
      }

      "Built-in method" - { // This is here for documentation.
        testBoth(
          Vector(
            () => Logger(InnerPackage).withMinimumLevel(Level.Debug).replace(),
            () => Logger(BarPath).withMinimumLevel(Level.Info).replace(),
          ),
        )
      }
      "ScribeUtils" - {
        testBoth(
          Vector(
            () => ScribeUtils.setLevel(InnerPackage, Level.Debug),
            () => ScribeUtils.setLevel(BarPath, Level.Info),
          ),
        )
      }
    }

    "Can override (more permissive)" - {
      def test(setLevels: Seq[() => Any]): Assertion = {
        setLevels.foreach(_())
        logDebug()
        logs.toVector shouldReturn Vector("Bar debug")
      }

      "Built-in method" in { // This is here for documentation.
        test(
          Vector(
            () => Logger(BarPath).withMinimumLevel(Level.Info).replace(),
            () => Logger(BarPath).withMinimumLevel(Level.Debug).replace(),
          ),
        )
      }
      "ScribeUtils" in {
        test(
          Vector(
            () => ScribeUtils.setLevel(BarPath, Level.Info),
            () => ScribeUtils.setLevel(BarPath, Level.Debug),
          ),
        )
      }
    }

    "Can override (less permissive)" - {
      def test(setLevels: Seq[() => Any]): Assertion = {
        setLevels.foreach(_())
        logDebug()
        logs.toVector shouldBe empty
      }

      "Built-in method" in { // This is here for documentation.
        test(
          Vector(
            () => Logger(BarPath).withMinimumLevel(Level.Debug).replace(),
            () => Logger(BarPath).withMinimumLevel(Level.Info).replace(),
          ),
        )
      }
      "ScribeUtils" in {
        test(
          Vector(
            () => ScribeUtils.setLevel(BarPath, Level.Debug),
            () => ScribeUtils.setLevel(BarPath, Level.Info),
          ),
        )
      }
    }

    "Can disable all logs" in {
      ScribeUtils.setLevel(BarPath, Level.Trace)
      ScribeUtils.noLogs()
      logDebug()
      logs.toVector shouldBe empty
    }

    "Works on package level (more permissive)" in {
      ScribeUtils.setLevel(BasePackage, Level.Fatal)
      ScribeUtils.setLevel(InnerPackage, Level.Debug)
      logInfo()
      logDebug()
      logs.toVector shouldReturn Vector("Foo info", "Bar debug")
    }
    "Works on package level (less permissive)" in {
      ScribeUtils.setLevel(BasePackage, Level.Debug)
      ScribeUtils.setLevel(InnerPackage, Level.Fatal)
      logInfo()
      logDebug()
      logs.toVector shouldBe empty
    }
  }
}

private object ScribeUtilsTest {
  private val BasePackage = "backend"
  private val InnerPackage = s"$BasePackage.logging"
  private val BarPath = s"$InnerPackage.Bar"
  private def logInfo(): Unit = new Foo().info()
  private def logDebug(): Unit = new Bar().debug()
}
